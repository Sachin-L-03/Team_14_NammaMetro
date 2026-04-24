package com.nammametro.repository;

import com.nammametro.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Report entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for reports.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByGeneratedById(Long userId);

    List<Report> findByType(String type);
}
