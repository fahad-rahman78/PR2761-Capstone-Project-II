package com.capstone.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * A person who can make bookings. Maps to the "users" table.
 *
 * This is a JPA entity: each field maps to a column, and Hibernate handles the
 * SQL for us. We keep it deliberately simple - the project's focus is
 * concurrency, not user management, so there is no password/auth here.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches BIGSERIAL in Postgres
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // JPA requires a no-args constructor.
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // --- getters and setters ---

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
