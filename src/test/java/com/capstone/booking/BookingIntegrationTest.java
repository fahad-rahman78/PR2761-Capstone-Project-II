package com.capstone.booking;

import com.capstone.booking.model.Resource;
import com.capstone.booking.model.User;
import com.capstone.booking.repository.ResourceRepository;
import com.capstone.booking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests: real Spring context, real (H2) database, real
 * HTTP routing via MockMvc. These prove the layers wire together correctly and
 * the right status codes come back.
 *
 * The "test" profile (application-test.properties) points us at H2 and turns
 * the PostgreSQL advisory lock off. These tests check FUNCTIONAL behaviour;
 * the concurrency guarantee under load is proven separately with JMeter.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResourceRepository resourceRepository;

    private Long userId;
    private Long resourceId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("Test User", "test+" + System.nanoTime() + "@example.com"));
        Resource resource = resourceRepository.save(new Resource("Test Room", "meeting_room", 1));
        userId = user.getUserId();
        resourceId = resource.getResourceId();
    }

    private String bookingJson(String start, String end) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "resourceId", resourceId,
                "startTime", start,
                "endTime", end));
    }

    @Test
    void validBooking_returns201() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.bookingId").exists());
    }

    @Test
    void conflictingBooking_returns409() throws Exception {
        // First booking succeeds.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isCreated());

        // Second booking for the same slot must be rejected.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BOOKING_CONFLICT"));
    }

    @Test
    void adjacentBooking_isAllowed() throws Exception {
        // 14:00-15:00 then 15:00-16:00 do NOT overlap (end is exclusive).
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T15:00:00", "2026-06-15T16:00:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void cancelledBooking_freesTheSlot() throws Exception {
        // Book, cancel, then re-book the same slot - should succeed again.
        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(response).get("bookingId").asLong();

        mockMvc.perform(delete("/api/bookings/" + bookingId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson("2026-06-15T14:00:00", "2026-06-15T15:00:00")))
                .andExpect(status().isCreated());
    }
}
