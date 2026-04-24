package com.nammametro.service;

import com.nammametro.model.Operator;
import com.nammametro.repository.OperatorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Operator operations.
 * Implements IOperatorService (ISP) — only operator-specific methods.
 *
 * // SOLID Principle: Interface Segregation Principle (ISP)
 *
 * SRP: This class has one responsibility — encapsulating business rules for operators.
 */
@Service
public class OperatorService implements IOperatorService {

    private final OperatorRepository operatorRepository;

    public OperatorService(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    public List<Operator> findAll() {
        return operatorRepository.findAll();
    }

    public Optional<Operator> findById(Long id) {
        return operatorRepository.findById(id);
    }

    @Override
    public Optional<Operator> findByUserId(Long userId) {
        return operatorRepository.findByUserId(userId);
    }

    public Optional<Operator> findByEmployeeId(String employeeId) {
        return operatorRepository.findByEmployeeId(employeeId);
    }

    @Override
    public Operator save(Operator operator) {
        return operatorRepository.save(operator);
    }

    public void deleteById(Long id) {
        operatorRepository.deleteById(id);
    }
}
