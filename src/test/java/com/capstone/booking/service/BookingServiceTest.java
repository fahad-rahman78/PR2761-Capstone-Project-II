package com.capstone.booking.service;

import com.capstone.booking.dto.BookingRequest;
import com.capstone.booking.exception.BookingConflictException;
import com.capstone.booking.model.Booking;
import com.capstone.booking.model.BookingStatus;
import com.capstone.booking.model.Resource;
import com.capstone.booking.model.User;
import com.capstone.booking.repository.BookingRepository;
import com.capstone.booking.repository.ResourceRepository;
import com.capstone.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for BookingService. The repositories are mocked, so these
 * tests run in milliseconds and need no database at all. They check the
 * business RULES in isolation:
 *   - a free slot is booked
 *   - a full slot is rejected with a conflict
 *   - capacity > 1 allows multiple bookings up to the limit
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Resource exclusiveRoom;   // capacity 1

    @BeforeEach
    void setUp() {
        // Advisory lock is off in unit tests (no real PostgreSQL behind a mock).
        ReflectionTestUtils.setField(bookingService, "advisoryLockEnabled", false);

        user = new User("Alice", "alice@example.com");
        user.setUserId(1L);

        exclusiveRoom = new Resource("Meeting Room A", "meeting_room", 1);
        exclusiveRoom.setResourceId(10L);
    }

    private BookingRequest request() {
        BookingRequest req = new BookingRequest();
        req.setUserId(1L);
        req.setResourceId(10L);
        req.setStartTime(LocalDateTime.of(2026, 6, 15, 14, 0));
        req.setEndTime(LocalDateTime.of(2026, 6, 15, 15, 0));
        return req;
    }

    @Test
    void validRequest_isSavedSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(exclusiveRoom));
        when(bookingRepository.countOverlapping(anyLong(), any(), any(),
                any())).thenReturn(0L);                       // slot is free
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(request());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).saveAndFlush(any(Booking.class));
    }

    @Test
    void overlappingRequest_onCapacityOneResource_throwsConflict() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(exclusiveRoom));
        when(bookingRepository.countOverlapping(anyLong(), any(), any(),
                any())).thenReturn(1L);                       // already 1 booking, capacity 1

        assertThatThrownBy(() -> bookingService.createBooking(request()))
                .isInstanceOf(BookingConflictException.class);

        // It must NOT attempt to save when the slot is full.
        verify(bookingRepository, never()).saveAndFlush(any(Booking.class));
    }

    @Test
    void capacityThree_allowsSecondBooking() {
        Resource pod = new Resource("Group Study Pod", "study_pod", 3);
        pod.setResourceId(20L);

        BookingRequest req = request();
        req.setResourceId(20L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(pod));
        when(bookingRepository.countOverlapping(anyLong(), any(), any(),
                any())).thenReturn(1L);                       // 1 of 3 taken -> still room
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(req);

        assertThat(result).isNotNull();
        verify(bookingRepository).saveAndFlush(any(Booking.class));
    }

    @Test
    void endBeforeStart_throwsBadInput() {
        BookingRequest req = request();
        req.setEndTime(req.getStartTime().minusHours(1));     // invalid window

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
