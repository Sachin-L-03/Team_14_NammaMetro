package com.nammametro.service;

import com.nammametro.model.Log;
import com.nammametro.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Log (audit trail) operations.
 *
 * SRP: This class has one responsibility — encapsulating business rules for audit logs.
 */
@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> findAll() {
        return logRepository.findAll();
    }

    public Optional<Log> findById(Long id) {
        return logRepository.findById(id);
    }

    public List<Log> findByUserId(Long userId) {
        return logRepository.findByUserId(userId);
    }

    public List<Log> findByEntity(String entity) {
        return logRepository.findByEntity(entity);
    }

    public List<Log> findByAction(String action) {
        return logRepository.findByAction(action);
    }

    public Log save(Log log) {
        return logRepository.save(log);
    }

    public void deleteById(Long id) {
        logRepository.deleteById(id);
    }
}
