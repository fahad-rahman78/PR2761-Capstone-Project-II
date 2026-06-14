package com.capstone.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Something that can be booked - a meeting room, a doctor's slot, a tennis
 * court, etc. Maps to the "resources" table.
 *
 * NOTE on `capacity`:
 *   capacity = how many CONFIRMED bookings may overlap the same time window.
 *   - capacity = 1  -> classic exclusive booking (the double-booking demo).
 *   - capacity = N  -> up to N people may hold the same slot (e.g. a class).
 *
 *   In the original design the database EXCLUDE constraint blocked *all*
 *   overlaps, which silently ignored capacity > 1. We fixed that: the
 *   capacity rule is now enforced in the service inside a per-resource lock
 *   (see BookingService), so capacity actually means something.
 */
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Integer capacity = 1;

    public Resource() {
    }

    public Resource(String name, String type, Integer capacity) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    // --- getters and setters ---

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
