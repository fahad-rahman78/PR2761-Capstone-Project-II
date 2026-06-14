package com.capstone.booking.service;

import com.capstone.booking.dto.SlotResponse;
import com.capstone.booking.exception.ResourceNotFoundException;
import com.capstone.booking.model.BookingStatus;
import com.capstone.booking.model.Resource;
import com.capstone.booking.repository.BookingRepository;
import com.capstone.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only logic about resources: listing them, and working out which
 * one-hour slots on a given day are still bookable.
 */
@Service
public class ResourceService {

    // Slots run on the hour, from 09:00 up to (but not including) 17:00.
    private static final int OPEN_HOUR = 9;
    private static final int CLOSE_HOUR = 17;

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           BookingRepository bookingRepository) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * Builds the list of hourly slots for one resource on one date and marks
     * each as available or not. "Available" means the count of overlapping
     * confirmed bookings is still below the resource's capacity.
     */
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlots(Long resourceId, LocalDate date) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource " + resourceId + " not found"));

        List<SlotResponse> slots = new ArrayList<>();
        for (int hour = OPEN_HOUR; hour < CLOSE_HOUR; hour++) {
            LocalDateTime start = date.atTime(hour, 0);
            LocalDateTime end = start.plusHours(1);

            long booked = bookingRepository.countOverlapping(
                    resourceId, start, end, BookingStatus.CONFIRMED);

            boolean available = booked < resource.getCapacity();
            slots.add(new SlotResponse(start, end, available, booked,
                    resource.getCapacity()));
        }
        return slots;
    }
}
