package com.capstone.booking.service;

import com.capstone.booking.dto.BookingRequest;
import com.capstone.booking.exception.BookingConflictException;
import com.capstone.booking.exception.ResourceNotFoundException;
import com.capstone.booking.model.Booking;
import com.capstone.booking.model.BookingStatus;
import com.capstone.booking.model.Resource;
import com.capstone.booking.model.User;
import com.capstone.booking.repository.BookingRepository;
import com.capstone.booking.repository.ResourceRepository;
import com.capstone.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for bookings. This is where the project's central problem -
 * preventing double-bookings under concurrent load - is actually solved.
 *
 * The strategy has THREE layers of defence, from outer to inner:
 *
 *   1. Advisory lock (pessimistic locking that works on phantom rows).
 *      pg_advisory_xact_lock(resourceId) serialises every booking attempt for
 *      the same resource. Two racing requests for the same room now run one
 *      after the other, not at the same time. Crucially this works even when
 *      no booking rows exist yet - which is exactly where plain
 *      SELECT ... FOR UPDATE fails.
 *
 *   2. Capacity check inside the lock. We count overlapping CONFIRMED bookings
 *      and reject if we are already at capacity. Because we hold the lock, the
 *      count cannot change underneath us between the check and the insert.
 *
 *   3. Database safety-net (db/hardening.sql). For capacity-1 resources an
 *      EXCLUDE constraint refuses overlapping inserts at the database level.
 *      If application logic ever had a bug, the database itself would still
 *      reject the bad row - we catch that here and report it as a conflict.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Advisory locks are a PostgreSQL feature. We switch them off for the H2
     * test profile (H2 has no such function). Defaults to true for production.
     */
    @Value("${app.advisory-lock.enabled:true}")
    private boolean advisoryLockEnabled;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          ResourceRepository resourceRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * Atomically check availability and create a booking.
     *
     * Everything inside this method runs in ONE transaction (@Transactional).
     * If we throw at any point, Spring rolls the whole thing back, so we never
     * leave a half-written booking behind.
     */
    @Transactional
    public Booking createBooking(BookingRequest request) {

        // --- Step 0: basic validation that does not need the database ---
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        // --- Step 1: make sure the user and resource actually exist ---
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + request.getUserId() + " not found"));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource " + request.getResourceId() + " not found"));

        // --- Step 2: take the per-resource lock (pessimistic locking) ---
        // From here until the transaction ends, no other booking for THIS
        // resource can run its own check-and-insert. This closes the race.
        if (advisoryLockEnabled) {
            bookingRepository.acquireResourceLock(resource.getResourceId());
        }

        // --- Step 3: capacity check, safe because we hold the lock ---
        long overlapping = bookingRepository.countOverlapping(
                resource.getResourceId(),
                request.getStartTime(),
                request.getEndTime(),
                BookingStatus.CONFIRMED);

        if (overlapping >= resource.getCapacity()) {
            throw new BookingConflictException(
                    "The requested time slot is no longer available.");
        }

        // --- Step 4: safe to book ---
        Booking booking = new Booking(user, resource,
                request.getStartTime(), request.getEndTime());
        booking.setCreatedAt(LocalDateTime.now());

        try {
            return bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            // Layer 3: the database constraint caught something the app logic
            // missed (should be unreachable in normal operation, but this is
            // the guarantee that makes double-bookings truly impossible).
            throw new BookingConflictException(
                    "The requested time slot is no longer available.");
        }
    }

    /** Cancel a booking by id (sets status to CANCELLED, freeing the slot). */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking " + bookingId + " not found"));
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking " + bookingId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUser_UserIdOrderByStartTimeDesc(userId);
    }
}
