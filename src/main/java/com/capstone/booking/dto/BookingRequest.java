package com.capstone.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * The JSON body the client sends to POST /api/bookings.
 *
 * Using a DTO (Data Transfer Object) instead of the Booking entity directly
 * means the client only sends the four fields it is allowed to set - it cannot
 * smuggle in a bookingId or a status. The @NotNull annotations are checked
 * automatically before the controller method runs.
 */
public class BookingRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
