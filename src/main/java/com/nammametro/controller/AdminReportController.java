package com.nammametro.controller;

import com.nammametro.service.AdminService;
import com.nammametro.service.RouteService;
import com.nammametro.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

/**
 * Admin report controller — generates revenue and usage reports
 * with Chart.js visualization.
 *
 * SRP: This class has one responsibility — handling admin report requests.
 */
@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final AdminService adminService;
    private final UserService userService;
    private final RouteService routeService;

    public AdminReportController(AdminService adminService,
                                  UserService userService,
                                  RouteService routeService) {
        this.adminService = adminService;
        this.userService = userService;
        this.routeService = routeService;
    }

    /** Revenue report page */
    @GetMapping("/revenue")
    public String revenueReport(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 @RequestParam(required = false) Long routeId,
                                 Authentication authentication,
                                 Model model) {

        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedRouteId", routeId);

        // Get revenue data grouped by day
        Map<String, BigDecimal> revenueByDay = adminService.getRevenueByDay(startDate, endDate, routeId);
        model.addAttribute("revenueByDay", revenueByDay);

        // Chart data
        model.addAttribute("chartLabels", new ArrayList<>(revenueByDay.keySet()));
        model.addAttribute("chartValues", new ArrayList<>(revenueByDay.values()));

        // Total revenue
        BigDecimal total = revenueByDay.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", total);

        // Save report if data requested
        if (startDate != null || endDate != null) {
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
                userService.findByEmail(ud.getUsername()).ifPresent(user ->
                        adminService.generateRevenueReport(user, startDate, endDate, routeId));
            }
        }

        return "admin/reports/revenue";
    }

    /** Usage report page */
    @GetMapping("/usage")
    public String usageReport(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Authentication authentication,
                               Model model) {

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Tickets per route
        Map<String, Long> ticketsPerRoute = adminService.getTicketsPerRoute(startDate, endDate);
        model.addAttribute("ticketsPerRoute", ticketsPerRoute);
        model.addAttribute("routeLabels", new ArrayList<>(ticketsPerRoute.keySet()));
        model.addAttribute("routeValues", new ArrayList<>(ticketsPerRoute.values()));

        // Busiest stations
        Map<String, Long> busiestStations = adminService.getBusiestStations(startDate, endDate);
        model.addAttribute("busiestStations", busiestStations);
        model.addAttribute("stationLabels", new ArrayList<>(busiestStations.keySet()));
        model.addAttribute("stationValues", new ArrayList<>(busiestStations.values()));

        // Peak hours
        Map<Integer, Long> peakHours = adminService.getPeakHours(startDate, endDate);
        model.addAttribute("peakHours", peakHours);
        model.addAttribute("hourLabels", new ArrayList<>(peakHours.keySet()));
        model.addAttribute("hourValues", new ArrayList<>(peakHours.values()));

        // Save report
        if (startDate != null || endDate != null) {
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
                userService.findByEmail(ud.getUsername()).ifPresent(user ->
                        adminService.generateUsageReport(user, startDate, endDate));
            }
        }

        return "admin/reports/usage";
    }
}
