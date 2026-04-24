package com.nammametro.controller;

import com.nammametro.model.Train;
import com.nammametro.model.enums.TrainStatus;
import com.nammametro.service.AuditLogService;
import com.nammametro.service.RouteService;
import com.nammametro.service.TrainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin CRUD controller for Trains.
 * All endpoints are under /admin/trains/** and require ROLE_ADMIN.
 *
 * SRP: This class has one responsibility — handling admin train management requests.
 */
@Controller
@RequestMapping("/admin/trains")
public class AdminTrainController {

    private final TrainService trainService;
    private final RouteService routeService;
    private final AuditLogService auditLogService;

    public AdminTrainController(TrainService trainService,
                                 RouteService routeService,
                                 AuditLogService auditLogService) {
        this.trainService = trainService;
        this.routeService = routeService;
        this.auditLogService = auditLogService;
    }

    /** List all trains */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("trains", trainService.findAll());
        return "admin/trains/list";
    }

    /** Show create form */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("train", new Train());
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("statuses", TrainStatus.values());
        model.addAttribute("isEdit", false);
        return "admin/trains/form";
    }

    /** Show edit form */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return trainService.findById(id)
                .map(train -> {
                    model.addAttribute("train", train);
                    model.addAttribute("routes", routeService.findAll());
                    model.addAttribute("statuses", TrainStatus.values());
                    model.addAttribute("isEdit", true);
                    return "admin/trains/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Train not found.");
                    return "redirect:/admin/trains";
                });
    }

    /** Create or update a train */
    @PostMapping
    public String save(@RequestParam String trainNumber,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Long routeId,
                       @RequestParam String status,
                       @RequestParam Integer capacity,
                       @RequestParam(required = false) Long trainId,
                       @RequestParam(required = false) Boolean isEdit,
                       RedirectAttributes redirectAttributes) {

        // Validation: unique train number
        if (Boolean.TRUE.equals(isEdit) && trainId != null) {
            if (trainService.existsByTrainNumberExcludingId(trainNumber, trainId)) {
                redirectAttributes.addFlashAttribute("error",
                        "Train number '" + trainNumber + "' is already in use.");
                return "redirect:/admin/trains/" + trainId + "/edit";
            }
        } else {
            if (trainService.existsByTrainNumber(trainNumber)) {
                redirectAttributes.addFlashAttribute("error",
                        "Train number '" + trainNumber + "' is already in use.");
                return "redirect:/admin/trains/new";
            }
        }

        // Build train
        Train train;
        if (Boolean.TRUE.equals(isEdit) && trainId != null) {
            train = trainService.findById(trainId).orElse(new Train());
        } else {
            train = new Train();
        }

        train.setTrainNumber(trainNumber);
        train.setName(name);
        train.setStatus(TrainStatus.valueOf(status));
        train.setCapacity(capacity);

        // Assign route (optional)
        if (routeId != null) {
            routeService.findById(routeId).ifPresent(train::setRoute);
        } else {
            train.setRoute(null);
        }

        Train saved = trainService.save(train);

        // Audit log
        String action = Boolean.TRUE.equals(isEdit) ? "UPDATE" : "CREATE";
        auditLogService.logAction(action, "Train", saved.getId(),
                action + " train: " + saved.getTrainNumber() + " — " + saved.getStatus());

        redirectAttributes.addFlashAttribute("success",
                "Train " + (Boolean.TRUE.equals(isEdit) ? "updated" : "created") + " successfully.");
        return "redirect:/admin/trains";
    }

    /** Delete a train */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        trainService.findById(id).ifPresent(train -> {
            auditLogService.logAction("DELETE", "Train", id,
                    "DELETE train: " + train.getTrainNumber());
        });
        trainService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Train deleted successfully.");
        return "redirect:/admin/trains";
    }
}
