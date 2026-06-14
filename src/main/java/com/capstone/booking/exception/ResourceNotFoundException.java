package com.capstone.booking.exception;

/**
 * Thrown when a request refers to a user, resource, or booking id that does
 * not exist. The GlobalExceptionHandler maps it to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
