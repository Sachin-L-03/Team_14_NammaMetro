package com.nammametro.service;

import com.nammametro.model.Log;
import com.nammametro.model.Report;
import com.nammametro.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ============================================================
 *  SOLID Principle: Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  This interface defines only admin-specific operations:
 *  report generation, log management, and user management.
 *  Admin does NOT depend on passenger or operator methods.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for admin-specific actions.
 * ============================================================
 */
// SOLID Principle: Interface Segregation Principle (ISP)
public interface IAdminService {

    /** Generate a revenue report */
    Report generateRevenueReport(User admin, String startDate, String endDate, Long routeId);

    /** Generate a usage report */
    Report generateUsageReport(User admin, String startDate, String endDate);

    /** Get paginated system logs */
    Page<Log> getLogsPage(int page, int size, String actionFilter);

    /** Get all distinct action types for filter dropdown */
    List<String> getDistinctActions();
}
