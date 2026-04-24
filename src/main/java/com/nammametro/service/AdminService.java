package com.nammametro.service;

import com.nammametro.model.*;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic layer for Admin operations.
 * Implements IAdminService (ISP) — only admin-specific methods:
 * report generation, log management, user management.
 *
 * // SOLID Principle: Interface Segregation Principle (ISP)
 *
 * SRP: This class has one responsibility — encapsulating admin business rules.
 */
@Service
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final TicketRepository ticketRepository;
    private final LogRepository logRepository;
    private final ReportRepository reportRepository;
    private final RouteRepository routeRepository;

    public AdminService(AdminRepository adminRepository,
                         TicketRepository ticketRepository,
                         LogRepository logRepository,
                         ReportRepository reportRepository,
                         RouteRepository routeRepository) {
        this.adminRepository = adminRepository;
        this.ticketRepository = ticketRepository;
        this.logRepository = logRepository;
        this.reportRepository = reportRepository;
        this.routeRepository = routeRepository;
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> findByUserId(Long userId) {
        return adminRepository.findByUserId(userId);
    }

    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }

    public void deleteById(Long id) {
        adminRepository.deleteById(id);
    }

    // ========================= IAdminService methods =========================

    /**
     * Generates a revenue report: total fare collected grouped by day.
     * Saved to the reports table.
     */
    @Override
    public Report generateRevenueReport(User admin, String startDate, String endDate, Long routeId) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        List<Ticket> allTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .filter(t -> {
                    if (t.getBookingTime() == null) return false;
                    LocalDate d = t.getBookingTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .filter(t -> {
                    if (routeId == null || routeId == 0) return true;
                    return t.getSchedule() != null
                            && t.getSchedule().getTrain() != null
                            && t.getSchedule().getTrain().getRoute() != null
                            && t.getSchedule().getTrain().getRoute().getId().equals(routeId);
                })
                .toList();

        // Save report
        Report report = new Report();
        report.setGeneratedBy(admin);
        report.setTitle("Revenue Report: " + start + " to " + end);
        report.setType("REVENUE");
        report.setParameters("{\"startDate\":\"" + start + "\",\"endDate\":\"" + end
                + "\",\"routeId\":" + (routeId != null ? routeId : "null") + "}");
        report.setContent("Total tickets: " + allTickets.size()
                + ", Total revenue: ₹" + allTickets.stream()
                .map(Ticket::getFare).reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setGeneratedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    /**
     * Generates a usage report: tickets per route, busiest stations, peak hours.
     */
    @Override
    public Report generateUsageReport(User admin, String startDate, String endDate) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        Report report = new Report();
        report.setGeneratedBy(admin);
        report.setTitle("Usage Report: " + start + " to " + end);
        report.setType("USAGE");
        report.setParameters("{\"startDate\":\"" + start + "\",\"endDate\":\"" + end + "\"}");
        report.setContent("Usage report generated for period " + start + " to " + end);
        report.setGeneratedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    public Page<Log> getLogsPage(int page, int size, String actionFilter) {
        if (actionFilter != null && !actionFilter.isEmpty()) {
            return logRepository.findByActionOrderByCreatedAtDesc(
                    actionFilter, PageRequest.of(page, size));
        }
        return logRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Override
    public List<String> getDistinctActions() {
        return logRepository.findDistinctActions();
    }

    // ========================= Revenue data for Chart.js =========================

    /**
     * Returns revenue data grouped by day for Chart.js.
     * Each map entry: date → total fare.
     */
    public Map<String, BigDecimal> getRevenueByDay(String startDate, String endDate, Long routeId) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .filter(t -> t.getBookingTime() != null)
                .filter(t -> {
                    LocalDate d = t.getBookingTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .filter(t -> {
                    if (routeId == null || routeId == 0) return true;
                    return t.getSchedule() != null
                            && t.getSchedule().getTrain() != null
                            && t.getSchedule().getTrain().getRoute() != null
                            && t.getSchedule().getTrain().getRoute().getId().equals(routeId);
                })
                .collect(Collectors.groupingBy(
                        t -> t.getBookingTime().toLocalDate().format(fmt),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Ticket::getFare, BigDecimal::add)
                ));
    }

    /**
     * Returns ticket count per route for usage chart.
     */
    public Map<String, Long> getTicketsPerRoute(String startDate, String endDate) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        return ticketRepository.findAll().stream()
                .filter(t -> t.getBookingTime() != null)
                .filter(t -> {
                    LocalDate d = t.getBookingTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .filter(t -> t.getSchedule() != null
                        && t.getSchedule().getTrain() != null
                        && t.getSchedule().getTrain().getRoute() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getSchedule().getTrain().getRoute().getName(),
                        Collectors.counting()
                ));
    }

    /**
     * Returns busiest stations (by ticket count as source or dest).
     */
    public Map<String, Long> getBusiestStations(String startDate, String endDate) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        Map<String, Long> stationCounts = new TreeMap<>();

        ticketRepository.findAll().stream()
                .filter(t -> t.getBookingTime() != null)
                .filter(t -> {
                    LocalDate d = t.getBookingTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .forEach(t -> {
                    if (t.getSourceStation() != null) {
                        stationCounts.merge(t.getSourceStation().getName(), 1L, Long::sum);
                    }
                    if (t.getDestStation() != null) {
                        stationCounts.merge(t.getDestStation().getName(), 1L, Long::sum);
                    }
                });

        return stationCounts;
    }

    /**
     * Returns peak hours (bookings grouped by hour of day).
     */
    public Map<Integer, Long> getPeakHours(String startDate, String endDate) {
        LocalDate start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate) : LocalDate.now();

        return ticketRepository.findAll().stream()
                .filter(t -> t.getBookingTime() != null)
                .filter(t -> {
                    LocalDate d = t.getBookingTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .collect(Collectors.groupingBy(
                        t -> t.getBookingTime().getHour(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
}
