package com.capstone.booking.controller;

import com.capstone.booking.dto.ResourceResponse;
import com.capstone.booking.dto.SlotResponse;
import com.capstone.booking.service.ResourceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST endpoints for browsing resources and their available slots.
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /** List every bookable resource. */
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> listResources() {
        List<ResourceResponse> resources = resourceService.getAllResources()
                .stream()
                .map(ResourceResponse::from)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Available slots for a resource on a given date.
     * Example: GET /api/resources/3/slots?date=2026-06-15
     * If no date is supplied we default to today.
     */
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponse>> getSlots(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(resourceService.getSlots(id, target));
    }
}
