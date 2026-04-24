package com.nammametro.service;

import com.nammametro.model.Train;
import com.nammametro.model.enums.TrainStatus;
import com.nammametro.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Train operations.
 * Implements ITrainService (ISP — Interface Segregation Principle).
 *
 * SRP: This class has one responsibility — encapsulating business rules for trains.
 */
@Service
public class TrainService implements ITrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    @Override
    public List<Train> findAll() {
        return trainRepository.findAll();
    }

    @Override
    public Optional<Train> findById(Long id) {
        return trainRepository.findById(id);
    }

    @Override
    public Optional<Train> findByTrainNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber);
    }

    @Override
    public List<Train> findByStatus(TrainStatus status) {
        return trainRepository.findByStatus(status);
    }

    @Override
    public Train save(Train train) {
        return trainRepository.save(train);
    }

    @Override
    public void deleteById(Long id) {
        trainRepository.deleteById(id);
    }

    @Override
    public boolean existsByTrainNumber(String trainNumber) {
        return trainRepository.existsByTrainNumber(trainNumber);
    }

    /**
     * Checks if a train with the given number exists, excluding a specific ID.
     * Used for update validation.
     */
    public boolean existsByTrainNumberExcludingId(String trainNumber, Long id) {
        return trainRepository.existsByTrainNumberAndIdNot(trainNumber, id);
    }

    @Override
    public long count() {
        return trainRepository.count();
    }
}
