package com.nammametro.service;

import com.nammametro.model.Incident;
import com.nammametro.model.enums.IncidentStatus;
import com.nammametro.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Incident operations.
 *
 * SRP: This class has one responsibility — encapsulating business rules for incidents.
 */
@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }

    public Optional<Incident> findById(Long id) {
        return incidentRepository.findById(id);
    }

    public List<Incident> findUnresolved() {
        return incidentRepository.findByResolvedFalse();
    }

    public List<Incident> findByTrainId(Long trainId) {
        return incidentRepository.findByTrainId(trainId);
    }

    public List<Incident> findBySeverity(String severity) {
        return incidentRepository.findBySeverity(severity);
    }

    public List<Incident> findByStatus(IncidentStatus status) {
        return incidentRepository.findByIncidentStatus(status);
    }

    public List<Incident> findOpen() {
        return incidentRepository.findByIncidentStatus(IncidentStatus.OPEN);
    }

    public long countOpen() {
        return incidentRepository.countByIncidentStatus(IncidentStatus.OPEN);
    }

    public Incident save(Incident incident) {
        return incidentRepository.save(incident);
    }

    public void deleteById(Long id) {
        incidentRepository.deleteById(id);
    }
}
