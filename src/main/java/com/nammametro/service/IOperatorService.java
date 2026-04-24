package com.nammametro.service;

import com.nammametro.model.Operator;
import java.util.Optional;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines only operator-specific operations.
 *  Operator does NOT depend on passenger or admin methods.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for operator-specific actions.
 * ============================================================
 */
// SOLID Principle: Interface Segregation Principle (ISP)
public interface IOperatorService {

    Optional<Operator> findByUserId(Long userId);

    Operator save(Operator operator);
}
