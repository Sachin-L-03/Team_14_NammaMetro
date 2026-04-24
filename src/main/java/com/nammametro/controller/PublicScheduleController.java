package com.nammametro.controller;

import com.nammametro.service.RouteService;
import com.nammametro.service.ScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Public schedule viewer — no login required.
 * Displays schedule table filterable by route.
 */
@Controller
@RequestMapping("/schedules/public")
public class PublicScheduleController {

    private final ScheduleService scheduleService;
    private final RouteService routeService;

    public PublicScheduleController(ScheduleService scheduleService, RouteService routeService) {
        this.scheduleService = scheduleService;
        this.routeService = routeService;
    }

    @GetMapping
    public String viewSchedules(@RequestParam(required = false) Long routeId, Model model) {
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("selectedRouteId", routeId);

        if (routeId != null && routeId > 0) {
            var allSchedules = scheduleService.findAll().stream()
                    .filter(s -> s.getTrain() != null
                            && s.getTrain().getRoute() != null
                            && s.getTrain().getRoute().getId().equals(routeId))
                    .toList();
            model.addAttribute("schedules", allSchedules);
        } else {
            model.addAttribute("schedules", scheduleService.findAll());
        }

        return "public/schedule-view";
    }
}
