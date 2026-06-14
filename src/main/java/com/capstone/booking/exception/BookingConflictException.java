package com.capstone.booking.exception;

/**
 * Thrown by the service when a requested slot cannot be booked because the
 * resource is already at capacity for that time window.
 *
 * It is a RuntimeException so that throwing it inside an @Transactional method
 * automatically rolls the transaction back. The GlobalExceptionHandler turns
 * it into a clean HTTP 409 Conflict response.
 */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
