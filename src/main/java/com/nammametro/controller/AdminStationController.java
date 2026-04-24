package com.nammametro.controller;

import com.nammametro.model.Station;
import com.nammametro.service.AuditLogService;
import com.nammametro.service.StationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin CRUD controller for Stations.
 * All endpoints are under /admin/stations/** and require ROLE_ADMIN.
 *
 * SRP: This class has one responsibility — handling admin station management requests.
 */
@Controller
@RequestMapping("/admin/stations")
public class AdminStationController {

    private final StationService stationService;
    private final AuditLogService auditLogService;

    public AdminStationController(StationService stationService,
                                   AuditLogService auditLogService) {
        this.stationService = stationService;
        this.auditLogService = auditLogService;
    }

    /** List all stations */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("stations", stationService.findAll());
        return "admin/stations/list";
    }

    /** Show create form */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("station", new Station());
        model.addAttribute("isEdit", false);
        return "admin/stations/form";
    }

    /** Show edit form */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return stationService.findById(id)
                .map(station -> {
                    model.addAttribute("station", station);
                    model.addAttribute("isEdit", true);
                    return "admin/stations/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Station not found.");
                    return "redirect:/admin/stations";
                });
    }

    /** Create or update a station */
    @PostMapping
    public String save(@ModelAttribute Station station,
                       @RequestParam(required = false) Boolean isEdit,
                       RedirectAttributes redirectAttributes) {

        // Validation: no duplicate station names
        if (Boolean.TRUE.equals(isEdit) && station.getId() != null) {
            if (stationService.existsByNameExcludingId(station.getName(), station.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "A station with the name '" + station.getName() + "' already exists.");
                return "redirect:/admin/stations/" + station.getId() + "/edit";
            }
            if (stationService.existsByCodeExcludingId(station.getCode(), station.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "A station with the code '" + station.getCode() + "' already exists.");
                return "redirect:/admin/stations/" + station.getId() + "/edit";
            }
        } else {
            if (stationService.existsByName(station.getName())) {
                redirectAttributes.addFlashAttribute("error",
                        "A station with the name '" + station.getName() + "' already exists.");
                return "redirect:/admin/stations/new";
            }
            if (stationService.existsByCode(station.getCode())) {
                redirectAttributes.addFlashAttribute("error",
                        "A station with the code '" + station.getCode() + "' already exists.");
                return "redirect:/admin/stations/new";
            }
        }

        Station saved = stationService.save(station);

        // Audit log
        String action = Boolean.TRUE.equals(isEdit) ? "UPDATE" : "CREATE";
        auditLogService.logAction(action, "Station", saved.getId(),
                action + " station: " + saved.getName() + " (" + saved.getCode() + ")");

        redirectAttributes.addFlashAttribute("success",
                "Station " + (Boolean.TRUE.equals(isEdit) ? "updated" : "created") + " successfully.");
        return "redirect:/admin/stations";
    }

    /** Delete a station */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        stationService.findById(id).ifPresent(station -> {
            auditLogService.logAction("DELETE", "Station", id,
                    "DELETE station: " + station.getName() + " (" + station.getCode() + ")");
        });
        stationService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Station deleted successfully.");
        return "redirect:/admin/stations";
    }
}
