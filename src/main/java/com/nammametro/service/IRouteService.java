package com.nammametro.service;

import com.nammametro.model.Route;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines ONLY route-specific operations.
 *  Admin module depends on this focused interface, not on
 *  passenger or operator service contracts.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for route business operations.
 * ============================================================
 */
public interface IRouteService {

    List<Route> findAll();

    Optional<Route> findById(Long id);

    Route save(Route route);

    void deleteById(Long id);

    long count();
}
