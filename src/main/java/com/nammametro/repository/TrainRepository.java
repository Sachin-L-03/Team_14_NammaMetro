package com.nammametro.repository;

import com.nammametro.model.Train;
import com.nammametro.model.enums.TrainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the Train entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for trains.
 */
@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    List<Train> findByStatus(TrainStatus status);

    boolean existsByTrainNumber(String trainNumber);

    boolean existsByTrainNumberAndIdNot(String trainNumber, Long id);
}
