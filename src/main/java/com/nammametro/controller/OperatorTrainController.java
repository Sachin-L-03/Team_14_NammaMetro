package com.nammametro.controller;

import com.nammametro.model.Train;
import com.nammametro.model.enums.TrainStatus;
import com.nammametro.pattern.TrainStateContext;
import com.nammametro.pattern.TrainStatusPublisher;
import com.nammametro.service.AuditLogService;
import com.nammametro.service.TrainCancellationService;
import com.nammametro.service.TrainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Operator controller for Train Status Updates.
 * Uses the State Pattern (TrainStateContext) to validate transitions.
 * Uses the Observer Pattern (TrainStatusPublisher) to notify observers on status change.
 * All endpoints under /operator/trains/** require ROLE_OPERATOR.
 *
 * SRP: This class has one responsibility — handling operator train status updates.
 */
@Controller
@RequestMapping("/operator/trains")
public class OperatorTrainController {

    private final TrainService trainService;
    private final AuditLogService auditLogService;
    private final TrainStatusPublisher trainStatusPublisher;
    private final TrainCancellationService trainCancellationService;

    public OperatorTrainController(TrainService trainService,
                                    AuditLogService auditLogService,
                                    TrainStatusPublisher trainStatusPublisher,
                                    TrainCancellationService trainCancellationService) {
        this.trainService = trainService;
        this.auditLogService = auditLogService;
        this.trainStatusPublisher = trainStatusPublisher;
        this.trainCancellationService = trainCancellationService;
    }

    /** List all trains with their statuses */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("statuses", TrainStatus.values());
        return "operator/trains/list";
    }

    /** Show status update form for a specific train */
    @GetMapping("/{id}/status")
    public String showStatusForm(@PathVariable Long id, Model model,
                                  RedirectAttributes redirectAttributes) {
        return trainService.findById(id)
                .map(train -> {
                    model.addAttribute("train", train);
                    model.addAttribute("statuses", TrainStatus.values());

                    // Behavioral Pattern: State Pattern
                    TrainStateContext stateContext = new TrainStateContext(train.getStatus());
                    model.addAttribute("currentStateName",
                            stateContext.getCurrentState().getStateName());

                    java.util.List<TrainStatus> allowedTransitions = new java.util.ArrayList<>();
                    for (TrainStatus ts : TrainStatus.values()) {
                        if (stateContext.canTransitionTo(ts)) {
                            allowedTransitions.add(ts);
                        }
                    }
                    model.addAttribute("allowedTransitions", allowedTransitions);

                    return "operator/trains/status";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Train not found.");
                    return "redirect:/operator/trains";
                });
    }

    /**
     * Update train status using the State Pattern.
     * After update, notifies all observers via the Observer Pattern
     * and triggers auto-cancellation if train is CANCELLED.
     *
     * // Behavioral Pattern: State Pattern
     * // Behavioral Pattern: Observer Pattern
     */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                                @RequestParam String newStatus,
                                RedirectAttributes redirectAttributes) {

        return trainService.findById(id)
                .map(train -> {
                    TrainStatus targetStatus = TrainStatus.valueOf(newStatus);

                    // Behavioral Pattern: State Pattern — validate transition
                    TrainStateContext stateContext = new TrainStateContext(train.getStatus());
                    try {
                        stateContext.transitionTo(targetStatus);
                    } catch (IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                        return "redirect:/operator/trains/" + id + "/status";
                    }

                    // Apply the new status
                    TrainStatus oldStatus = train.getStatus();
                    train.setStatus(targetStatus);
                    trainService.save(train);

                    // Audit log
                    auditLogService.logAction("STATUS_CHANGE", "Train", id,
                            "Train " + train.getTrainNumber()
                                    + " status changed: " + oldStatus + " → " + targetStatus);

                    // Behavioral Pattern: Observer Pattern — notify all observers
                    trainStatusPublisher.notifyObservers(train,
                            oldStatus.name(), targetStatus.name());

                    // If train is CANCELLED, auto-cancel all active tickets
                    if (targetStatus == TrainStatus.CANCELLED) {
                        int cancelled = trainCancellationService.autoCancelTicketsForTrain(train);
                        if (cancelled > 0) {
                            redirectAttributes.addFlashAttribute("info",
                                    cancelled + " tickets auto-cancelled with full refund.");
                        }
                    }

                    redirectAttributes.addFlashAttribute("success",
                            "Train " + train.getTrainNumber()
                                    + " status updated to " + targetStatus + ".");
                    return "redirect:/operator/trains";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Train not found.");
                    return "redirect:/operator/trains";
                });
    }
}
