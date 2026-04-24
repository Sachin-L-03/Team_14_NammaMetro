package com.nammametro.controller;

import com.nammametro.model.Incident;
import com.nammametro.model.User;
import com.nammametro.model.enums.IncidentStatus;
import com.nammametro.model.enums.Severity;
import com.nammametro.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Operator controller for Incident Management.
 * All endpoints under /operator/incidents/** require ROLE_OPERATOR.
 *
 * SRP: This class has one responsibility — handling operator incident management requests.
 */
@Controller
@RequestMapping("/operator/incidents")
public class OperatorIncidentController {

    private final IncidentService incidentService;
    private final TrainService trainService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public OperatorIncidentController(IncidentService incidentService,
                                       TrainService trainService,
                                       UserService userService,
                                       AuditLogService auditLogService) {
        this.incidentService = incidentService;
        this.trainService = trainService;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    /** List all incidents */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("incidents", incidentService.findAll());
        return "operator/incidents/list";
    }

    /** Show report incident form */
    @GetMapping("/report")
    public String showReportForm(Model model) {
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("severities", Severity.values());
        return "operator/incidents/report";
    }

    /** Submit a new incident report */
    @PostMapping("/report")
    public String reportIncident(@RequestParam String title,
                                  @RequestParam String type,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) Long trainId,
                                  @RequestParam String severity,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        Incident incident = new Incident();
        incident.setTitle(title);
        incident.setType(type);
        incident.setDescription(description);
        incident.setSeverity(Severity.valueOf(severity));
        incident.setIncidentStatus(IncidentStatus.OPEN);
        incident.setResolved(false);

        // Assign train if provided
        if (trainId != null) {
            trainService.findById(trainId).ifPresent(incident::setTrain);
        }

        // Set reporter from security context
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            userService.findByEmail(ud.getUsername()).ifPresent(incident::setReportedBy);
        }

        Incident saved = incidentService.save(incident);

        auditLogService.logAction("CREATE", "Incident", saved.getId(),
                "Reported incident: " + saved.getTitle() + " [" + saved.getSeverity() + "]");

        redirectAttributes.addFlashAttribute("success",
                "Incident reported successfully.");
        return "redirect:/operator/incidents";
    }

    /** Show resolve form */
    @GetMapping("/{id}/resolve")
    public String showResolveForm(@PathVariable Long id, Model model,
                                   RedirectAttributes redirectAttributes) {
        return incidentService.findById(id)
                .map(incident -> {
                    model.addAttribute("incident", incident);
                    return "operator/incidents/resolve";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Incident not found.");
                    return "redirect:/operator/incidents";
                });
    }

    /** Resolve an incident */
    @PostMapping("/{id}/resolve")
    public String resolveIncident(@PathVariable Long id,
                                   @RequestParam String resolutionNote,
                                   RedirectAttributes redirectAttributes) {
        return incidentService.findById(id)
                .map(incident -> {
                    incident.setIncidentStatus(IncidentStatus.RESOLVED);
                    incident.setResolved(true);
                    incident.setResolutionNote(resolutionNote);
                    incidentService.save(incident);

                    auditLogService.logAction("RESOLVE", "Incident", id,
                            "Resolved incident: " + incident.getTitle());

                    redirectAttributes.addFlashAttribute("success",
                            "Incident resolved successfully.");
                    return "redirect:/operator/incidents";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Incident not found.");
                    return "redirect:/operator/incidents";
                });
    }
}
