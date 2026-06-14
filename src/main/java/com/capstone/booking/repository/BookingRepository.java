package com.capstone.booking.repository;

import com.capstone.booking.model.Booking;
import com.capstone.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Database access for Booking rows, plus the two queries that make the
 * concurrency strategy work.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Counts how many CONFIRMED bookings on the given resource overlap the
     * requested window.
     *
     * Two half-open windows [s1,e1) and [s2,e2) overlap when:
     *     s1 < e2  AND  s2 < e1
     * which is what the WHERE clause below expresses. This is plain JPQL so it
     * runs identically on PostgreSQL (production) and H2 (tests).
     */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.resource.resourceId = :resourceId
              AND b.status = :status
              AND b.startTime < :endTime
              AND :startTime < b.endTime
            """)
    long countOverlapping(@Param("resourceId") Long resourceId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("status") BookingStatus status);

    /** Returns the overlapping bookings themselves (used by tests / debugging). */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.resource.resourceId = :resourceId
              AND b.status = :status
              AND b.startTime < :endTime
              AND :startTime < b.endTime
            """)
    List<Booking> findOverlapping(@Param("resourceId") Long resourceId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("status") BookingStatus status);

    /** All bookings belonging to one user, newest first. */
    List<Booking> findByUser_UserIdOrderByStartTimeDesc(Long userId);

    /**
     * Acquires a PostgreSQL transaction-level advisory lock keyed on the
     * resource id. This is the heart of the concurrency fix.
     *
     * Why we need it: `SELECT ... FOR UPDATE` only locks rows that ALREADY
     * exist. When two users race for a brand-new slot, both see zero rows,
     * both lock nothing, and both insert -> double booking. An advisory lock
     * is keyed on an arbitrary number (here, the resource id), so it works
     * even when there are no rows yet. The lock is released automatically when
     * the surrounding transaction commits or rolls back ("_xact_" variant).
     *
     * This is a PostgreSQL-only function, so it is only invoked when the
     * "app.advisory-lock.enabled" flag is true (off for the H2 test profile).
     */
    @Query(value = "SELECT pg_advisory_xact_lock(:key)", nativeQuery = true)
    void acquireResourceLock(@Param("key") long key);
}
