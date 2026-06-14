package com.capstone.booking.repository;

import com.capstone.booking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for Resource rows. Standard CRUD via JpaRepository.
 */
public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
