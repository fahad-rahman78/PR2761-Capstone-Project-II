package com.capstone.booking.dto;

import java.time.LocalDateTime;

/**
 * One bookable time slot returned by GET /api/resources/{id}/slots.
 *
 * `available` is true when the number of confirmed bookings overlapping this
 * slot is still below the resource's capacity.
 */
public record SlotResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean available,
        long booked,
        int capacity
) {
}
