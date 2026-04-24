package com.nammametro.repository;

import com.nammametro.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for the Passenger entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for passengers.
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    Optional<Passenger> findByUserId(Long userId);
}
