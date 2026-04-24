package com.nammametro.controller;

import com.nammametro.model.enums.TrainStatus;
import com.nammametro.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves role-specific dashboard pages.
 * Each dashboard is protected by Spring Security role-based access control
 * defined in SecurityConfig.
 *
 * SRP: This class has one responsibility — routing dashboard-related requests
 *      and populating dashboard view models.
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final IStationService stationService;
    private final IRouteService routeService;
    private final ITrainService trainService;
    private final PassengerService passengerService;
    private final ScheduleService scheduleService;
    private final IncidentService incidentService;
    private final TicketService ticketService;
    private final NotificationService notificationService;

    public DashboardController(UserService userService,
                               IStationService stationService,
                               IRouteService routeService,
                               ITrainService trainService,
                               PassengerService passengerService,
                               ScheduleService scheduleService,
                               IncidentService incidentService,
                               TicketService ticketService,
                               NotificationService notificationService) {
        this.userService = userService;
        this.stationService = stationService;
        this.routeService = routeService;
        this.trainService = trainService;
        this.passengerService = passengerService;
        this.scheduleService = scheduleService;
        this.incidentService = incidentService;
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    /**
     * Passenger dashboard — shows recent bookings, notifications, quick search.
     */
    @GetMapping("/passenger/dashboard")
    public String passengerDashboard(Authentication authentication, Model model) {
        populateUserModel(authentication, model);

        // Get passenger-specific data
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            userService.findByEmail(ud.getUsername()).ifPresent(user -> {
                passengerService.findByUserId(user.getId()).ifPresent(passenger -> {
                    model.addAttribute("passenger", passenger);
                    model.addAttribute("recentBookings",
                            ticketService.findRecentByPassengerId(passenger.getId()));
                });
                model.addAttribute("notifications",
                        notificationService.findRecentByUserId(user.getId()));
                model.addAttribute("unreadCount",
                        notificationService.countUnread(user.getId()));
            });
        }

        // Provide stations for the search form
        model.addAttribute("stations", stationService.findAll());

        return "dashboard/passenger";
    }

    /**
     * Operator dashboard — shows today's schedules, active incidents, problematic trains.
     */
    @GetMapping("/operator/dashboard")
    public String operatorDashboard(Authentication authentication, Model model) {
        populateUserModel(authentication, model);

        model.addAttribute("todaySchedules", scheduleService.findTodayActive());
        model.addAttribute("openIncidents", incidentService.findOpen());
        model.addAttribute("openIncidentCount", incidentService.countOpen());

        var delayedTrains = trainService.findByStatus(TrainStatus.DELAYED);
        var cancelledTrains = trainService.findByStatus(TrainStatus.CANCELLED);
        model.addAttribute("delayedTrains", delayedTrains);
        model.addAttribute("cancelledTrains", cancelledTrains);
        model.addAttribute("problemTrainCount",
                delayedTrains.size() + cancelledTrains.size());
        model.addAttribute("totalTrains", trainService.count());
        model.addAttribute("totalSchedules", scheduleService.count());

        return "dashboard/operator";
    }

    /**
     * Admin dashboard — shows entity counts for quick overview.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        populateUserModel(authentication, model);

        model.addAttribute("stationCount", stationService.count());
        model.addAttribute("routeCount", routeService.count());
        model.addAttribute("trainCount", trainService.count());
        model.addAttribute("passengerCount", passengerService.findAll().size());

        return "dashboard/admin";
    }

    private void populateUserModel(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            userService.findByEmail(email).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }
    }
}
