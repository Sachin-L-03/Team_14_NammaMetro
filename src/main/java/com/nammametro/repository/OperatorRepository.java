package com.nammametro.repository;

import com.nammametro.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for the Operator entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for operators.
 */
@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {

    Optional<Operator> findByUserId(Long userId);

    Optional<Operator> findByEmployeeId(String employeeId);
}
