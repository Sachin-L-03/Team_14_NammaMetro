package com.nammametro.repository;

import com.nammametro.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Route entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for routes.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /** Find routes that connect a source station to a destination station */
    @Query("SELECT r FROM Route r WHERE r.startStation.id = :sourceId AND r.endStation.id = :destId")
    List<Route> findByStations(@Param("sourceId") Long sourceId, @Param("destId") Long destId);

    /** Find routes where either start or end matches the given station */
    @Query("SELECT r FROM Route r WHERE r.startStation.id = :stationId OR r.endStation.id = :stationId")
    List<Route> findRoutesInvolvingStation(@Param("stationId") Long stationId);
}
