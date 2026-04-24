package com.nammametro.controller;

import com.nammametro.model.Route;
import com.nammametro.model.Schedule;
import com.nammametro.model.Station;
import com.nammametro.service.*;
import com.nammametro.service.fare.FareBreakdown;
import com.nammametro.service.fare.FareCalculator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles route search and fare estimation for passengers.
 *
 * SRP: This class has one responsibility — handling passenger route search requests.
 */
@Controller
@RequestMapping("/passenger/search")
public class PassengerSearchController {

    private final StationService stationService;
    private final RouteService routeService;
    private final ScheduleService scheduleService;
    private final FareCalculator fareCalculator;
    private final UserService userService;
    private final PassengerService passengerService;

    public PassengerSearchController(StationService stationService,
                                      RouteService routeService,
                                      ScheduleService scheduleService,
                                      FareCalculator fareCalculator,
                                      UserService userService,
                                      PassengerService passengerService) {
        this.stationService = stationService;
        this.routeService = routeService;
        this.scheduleService = scheduleService;
        this.fareCalculator = fareCalculator;
        this.userService = userService;
        this.passengerService = passengerService;
    }

    /** Show search form */
    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("stations", stationService.findAll());
        return "passenger/search";
    }

    /** Process search and show results */
    @PostMapping
    public String searchRoutes(@RequestParam Long sourceStationId,
                                @RequestParam Long destStationId,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (sourceStationId.equals(destStationId)) {
            redirectAttributes.addFlashAttribute("error",
                    "Source and destination must be different.");
            return "redirect:/passenger/search";
        }

        Station source = stationService.findById(sourceStationId).orElse(null);
        Station dest = stationService.findById(destStationId).orElse(null);

        if (source == null || dest == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid stations.");
            return "redirect:/passenger/search";
        }

        // Find matching routes
        List<Route> routes = routeService.findAll().stream()
                .filter(r -> {
                    boolean directMatch = (r.getStartStation().getId().equals(sourceStationId)
                            && r.getEndStation().getId().equals(destStationId));
                    boolean reverseMatch = (r.getStartStation().getId().equals(destStationId)
                            && r.getEndStation().getId().equals(sourceStationId));
                    return directMatch || reverseMatch;
                })
                .toList();

        // Determine metro card status
        boolean hasMetroCard = false;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            var userOpt = userService.findByEmail(ud.getUsername());
            if (userOpt.isPresent()) {
                var passengerOpt = passengerService.findByUserId(userOpt.get().getId());
                if (passengerOpt.isPresent()) {
                    hasMetroCard = Boolean.TRUE.equals(passengerOpt.get().getHasMetroCard());
                }
            }
        }

        // Calculate fare for each route
        List<FareBreakdown> fareBreakdowns = new ArrayList<>();
        for (Route route : routes) {
            BigDecimal distance = route.getDistanceKm() != null
                    ? route.getDistanceKm()
                    : new BigDecimal("5"); // default 5km if not set
            fareBreakdowns.add(fareCalculator.calculate(distance, hasMetroCard));
        }

        // Find schedules for the matching routes
        List<Schedule> availableSchedules = new ArrayList<>();
        for (Route route : routes) {
            // Get schedules for trains on this route
            route.getStartStation(); // ensure loaded
            var allSchedules = scheduleService.findAll();
            for (Schedule s : allSchedules) {
                if (s.getTrain() != null && s.getTrain().getRoute() != null
                        && s.getTrain().getRoute().getId().equals(route.getId())) {
                    availableSchedules.add(s);
                }
            }
        }

        model.addAttribute("source", source);
        model.addAttribute("dest", dest);
        model.addAttribute("routes", routes);
        model.addAttribute("fareBreakdowns", fareBreakdowns);
        model.addAttribute("schedules", availableSchedules);
        model.addAttribute("hasMetroCard", hasMetroCard);
        model.addAttribute("stations", stationService.findAll());

        return "passenger/search-results";
    }
}
