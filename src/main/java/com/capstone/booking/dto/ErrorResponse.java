package com.capstone.booking.dto;

/**
 * A consistent JSON shape for every error the API returns, e.g.
 *   { "error": "BOOKING_CONFLICT", "message": "The requested time slot ..." }
 *
 * Returning a predictable error object (rather than a raw stack trace) is what
 * lets the frontend show a friendly message to the user.
 */
public record ErrorResponse(String error, String message) {
}
