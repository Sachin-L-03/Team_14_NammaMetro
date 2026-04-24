package com.nammametro.service;

import com.nammametro.model.Station;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines ONLY station-specific operations.
 *  Clients that need station data depend on this focused interface
 *  rather than a bloated "god" service with all entity operations.
 *
 *  The Admin module depends on IStationService, IRouteService, and
 *  ITrainService — it never sees passenger-specific methods.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for station business operations.
 * ============================================================
 */
public interface IStationService {

    List<Station> findAll();

    Optional<Station> findById(Long id);

    Optional<Station> findByCode(String code);

    Optional<Station> findByName(String name);

    List<Station> findByLine(String line);

    List<Station> findActiveStations();

    Station save(Station station);

    void deleteById(Long id);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    long count();
}
