package com.nammametro.service;

import com.nammametro.model.Report;
import com.nammametro.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Report operations.
 *
 * SRP: This class has one responsibility — encapsulating business rules for reports.
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    public Optional<Report> findById(Long id) {
        return reportRepository.findById(id);
    }

    public List<Report> findByGeneratedById(Long userId) {
        return reportRepository.findByGeneratedById(userId);
    }

    public List<Report> findByType(String type) {
        return reportRepository.findByType(type);
    }

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    public void deleteById(Long id) {
        reportRepository.deleteById(id);
    }
}
