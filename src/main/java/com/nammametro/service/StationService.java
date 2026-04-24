package com.nammametro.service;

import com.nammametro.model.Station;
import com.nammametro.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Station operations.
 * Implements IStationService (ISP — Interface Segregation Principle).
 *
 * SRP: This class has one responsibility — encapsulating business rules for stations.
 */
@Service
public class StationService implements IStationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    @Override
    public Optional<Station> findById(Long id) {
        return stationRepository.findById(id);
    }

    @Override
    public Optional<Station> findByCode(String code) {
        return stationRepository.findByCode(code);
    }

    @Override
    public Optional<Station> findByName(String name) {
        return stationRepository.findByName(name);
    }

    @Override
    public List<Station> findByLine(String line) {
        return stationRepository.findByLine(line);
    }

    @Override
    public List<Station> findActiveStations() {
        return stationRepository.findByIsActiveTrue();
    }

    @Override
    public Station save(Station station) {
        return stationRepository.save(station);
    }

    @Override
    public void deleteById(Long id) {
        stationRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return stationRepository.existsByName(name);
    }

    @Override
    public boolean existsByCode(String code) {
        return stationRepository.existsByCode(code);
    }

    /**
     * Checks if a station with the given name exists, excluding a specific ID.
     * Used for update validation (allow the same station to keep its name).
     */
    public boolean existsByNameExcludingId(String name, Long id) {
        return stationRepository.existsByNameAndIdNot(name, id);
    }

    public boolean existsByCodeExcludingId(String code, Long id) {
        return stationRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public long count() {
        return stationRepository.count();
    }
}
