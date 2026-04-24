package com.nammametro.repository;

import com.nammametro.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the Station entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for stations.
 */
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByCode(String code);

    Optional<Station> findByName(String name);

    List<Station> findByLine(String line);

    List<Station> findByIsActiveTrue();

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}
