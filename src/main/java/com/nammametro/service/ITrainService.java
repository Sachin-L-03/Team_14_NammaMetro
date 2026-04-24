package com.nammametro.service;

import com.nammametro.model.Train;
import com.nammametro.model.enums.TrainStatus;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines ONLY train-specific operations.
 *  Admin module depends on this focused interface, segregated
 *  from passenger-facing or operator-facing contracts.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for train business operations.
 * ============================================================
 */
public interface ITrainService {

    List<Train> findAll();

    Optional<Train> findById(Long id);

    Optional<Train> findByTrainNumber(String trainNumber);

    List<Train> findByStatus(TrainStatus status);

    Train save(Train train);

    void deleteById(Long id);

    boolean existsByTrainNumber(String trainNumber);

    long count();
}
