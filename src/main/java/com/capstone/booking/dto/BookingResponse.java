package com.capstone.booking.dto;

import com.capstone.booking.model.Booking;

import java.time.LocalDateTime;

/**
 * The JSON we send back after a successful booking. Built from a Booking
 * entity by the static `from(...)` factory below, so the controller stays tidy.
 *
 * A Java record is a compact, immutable data carrier - perfect for responses.
 */
public record BookingResponse(
        Long bookingId,
        String status,
        Long resourceId,
        Long userId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getStatus().name(),
                booking.getResource().getResourceId(),
                booking.getUser().getUserId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCreatedAt()
        );
    }
}
