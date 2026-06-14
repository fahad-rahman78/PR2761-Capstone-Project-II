package com.capstone.booking.model;

/**
 * The lifecycle states a booking can be in.
 *
 * Only CONFIRMED bookings count towards a resource being "full". A CANCELLED
 * booking frees the slot up again, which is why our conflict check (and the
 * database safety-net) only look at CONFIRMED rows.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED
}
