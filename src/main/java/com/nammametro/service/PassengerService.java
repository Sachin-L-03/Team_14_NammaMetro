package com.nammametro.service;

import com.nammametro.model.Passenger;
import com.nammametro.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Passenger operations.
 * Implements IPassengerService (ISP) — only passenger-specific methods.
 *
 * // SOLID Principle: Interface Segregation Principle (ISP)
 *
 * SRP: This class has one responsibility — encapsulating business rules for passengers.
 */
@Service
public class PassengerService implements IPassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public List<Passenger> findAll() {
        return passengerRepository.findAll();
    }

    public Optional<Passenger> findById(Long id) {
        return passengerRepository.findById(id);
    }

    @Override
    public Optional<Passenger> findByUserId(Long userId) {
        return passengerRepository.findByUserId(userId);
    }

    @Override
    public Passenger save(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public void deleteById(Long id) {
        passengerRepository.deleteById(id);
    }
}
