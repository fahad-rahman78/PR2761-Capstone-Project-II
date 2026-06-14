package com.capstone.booking.repository;

import com.capstone.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for User rows.
 *
 * By extending JpaRepository we get findById, findAll, save, deleteById, etc.
 * for free - Spring Data writes the SQL at runtime, so there is no
 * implementation class to maintain.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
