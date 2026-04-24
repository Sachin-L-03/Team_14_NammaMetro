package com.nammametro.repository;

import com.nammametro.model.Incident;
import com.nammametro.model.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Incident entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for incidents.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByResolvedFalse();

    List<Incident> findByTrainId(Long trainId);

    List<Incident> findBySeverity(String severity);

    List<Incident> findByIncidentStatus(IncidentStatus status);

    long countByIncidentStatus(IncidentStatus status);
}
