package com.nammametro.controller;

import com.nammametro.model.Route;
import com.nammametro.model.Station;
import com.nammametro.service.AuditLogService;
import com.nammametro.service.RouteService;
import com.nammametro.service.StationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin CRUD controller for Routes.
 * All endpoints are under /admin/routes/** and require ROLE_ADMIN.
 *
 * SRP: This class has one responsibility — handling admin route management requests.
 */
@Controller
@RequestMapping("/admin/routes")
public class AdminRouteController {

    private final RouteService routeService;
    private final StationService stationService;
    private final AuditLogService auditLogService;

    public AdminRouteController(RouteService routeService,
                                 StationService stationService,
                                 AuditLogService auditLogService) {
        this.routeService = routeService;
        this.stationService = stationService;
        this.auditLogService = auditLogService;
    }

    /** List all routes */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("routes", routeService.findAll());
        return "admin/routes/list";
    }

    /** Show create form */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("route", new Route());
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/routes/form";
    }

    /** Show edit form */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return routeService.findById(id)
                .map(route -> {
                    model.addAttribute("route", route);
                    model.addAttribute("stations", stationService.findAll());
                    model.addAttribute("isEdit", true);
                    return "admin/routes/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Route not found.");
                    return "redirect:/admin/routes";
                });
    }

    /** Create or update a route */
    @PostMapping
    public String save(@RequestParam String name,
                       @RequestParam Long startStationId,
                       @RequestParam Long endStationId,
                       @RequestParam(required = false) List<Long> intermediateStationIds,
                       @RequestParam(required = false) java.math.BigDecimal distanceKm,
                       @RequestParam(required = false) Integer durationMin,
                       @RequestParam(required = false) Long routeId,
                       @RequestParam(required = false) Boolean isEdit,
                       RedirectAttributes redirectAttributes) {

        // Validation: source and destination must be different
        if (startStationId.equals(endStationId)) {
            redirectAttributes.addFlashAttribute("error",
                    "Source and destination stations must be different.");
            return Boolean.TRUE.equals(isEdit) && routeId != null
                    ? "redirect:/admin/routes/" + routeId + "/edit"
                    : "redirect:/admin/routes/new";
        }

        // Validation: stations must exist
        Station startStation = stationService.findById(startStationId).orElse(null);
        Station endStation = stationService.findById(endStationId).orElse(null);
        if (startStation == null || endStation == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Selected stations are invalid.");
            return "redirect:/admin/routes/new";
        }

        // Build route
        Route route;
        if (Boolean.TRUE.equals(isEdit) && routeId != null) {
            route = routeService.findById(routeId).orElse(new Route());
        } else {
            route = new Route();
        }

        route.setName(name);
        route.setStartStation(startStation);
        route.setEndStation(endStation);
        route.setDistanceKm(distanceKm);
        route.setDurationMin(durationMin);

        // Build intermediate stations list
        List<Station> intermediateStations = new ArrayList<>();
        if (intermediateStationIds != null) {
            for (Long stationId : intermediateStationIds) {
                stationService.findById(stationId).ifPresent(intermediateStations::add);
            }
        }
        route.setIntermediateStations(intermediateStations);

        Route saved = routeService.save(route);

        // Audit log
        String action = Boolean.TRUE.equals(isEdit) ? "UPDATE" : "CREATE";
        auditLogService.logAction(action, "Route", saved.getId(),
                action + " route: " + saved.getName()
                        + " (" + startStation.getName() + " → " + endStation.getName() + ")");

        redirectAttributes.addFlashAttribute("success",
                "Route " + (Boolean.TRUE.equals(isEdit) ? "updated" : "created") + " successfully.");
        return "redirect:/admin/routes";
    }

    /** Delete a route */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        routeService.findById(id).ifPresent(route -> {
            auditLogService.logAction("DELETE", "Route", id,
                    "DELETE route: " + route.getName());
        });
        routeService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Route deleted successfully.");
        return "redirect:/admin/routes";
    }
}
