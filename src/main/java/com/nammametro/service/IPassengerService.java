package com.nammametro.service;

import com.nammametro.model.Passenger;
import java.util.Optional;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines only passenger-specific operations.
 *  Passenger does NOT depend on admin or operator methods.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for passenger-specific actions.
 * ============================================================
 */
// SOLID Principle: Interface Segregation Principle (ISP)
public interface IPassengerService {

    /** Search route between source and destination */
    Optional<Passenger> findByUserId(Long userId);

    /** Book a ticket (delegated to TicketService) */
    Passenger save(Passenger passenger);
}
