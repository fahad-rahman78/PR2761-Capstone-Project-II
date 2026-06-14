package com.capstone.booking.controller;

import com.capstone.booking.dto.BookingRequest;
import com.capstone.booking.dto.BookingResponse;
import com.capstone.booking.model.Booking;
import com.capstone.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for bookings. The controller's only job is HTTP plumbing:
 * read the request, call the service, turn the result into a response with the
 * right status code. All the real logic lives in BookingService.
 */
@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Create a booking. Returns 201 on success, 409 if the slot is taken. */
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookingResponse.from(booking));
    }

    /** Fetch a single booking. Returns 200, or 404 if it does not exist. */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.getBooking(id)));
    }

    /** Cancel a booking. Returns 204 No Content on success. */
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    /** All bookings for one user. */
    @GetMapping("/users/{userId}/bookings")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsForUser(userId)
                .stream()
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity.ok(bookings);
    }
}
