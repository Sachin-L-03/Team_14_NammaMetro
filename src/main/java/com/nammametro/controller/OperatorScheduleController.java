package com.nammametro.controller;

import com.nammametro.model.Schedule;
import com.nammametro.model.enums.ScheduleStatus;
import com.nammametro.service.AuditLogService;
import com.nammametro.service.ScheduleService;
import com.nammametro.service.StationService;
import com.nammametro.service.TrainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Operator controller for Schedule management.
 * All endpoints under /operator/schedules/** require ROLE_OPERATOR.
 *
 * SRP: This class has one responsibility — handling operator schedule management requests.
 */
@Controller
@RequestMapping("/operator/schedules")
public class OperatorScheduleController {

    private final ScheduleService scheduleService;
    private final TrainService trainService;
    private final StationService stationService;
    private final AuditLogService auditLogService;

    public OperatorScheduleController(ScheduleService scheduleService,
                                       TrainService trainService,
                                       StationService stationService,
                                       AuditLogService auditLogService) {
        this.scheduleService = scheduleService;
        this.trainService = trainService;
        this.stationService = stationService;
        this.auditLogService = auditLogService;
    }

    /** List all schedules */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", scheduleService.findAll());
        return "operator/schedules/list";
    }

    /** Show create form */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("statuses", ScheduleStatus.values());
        model.addAttribute("isEdit", false);
        return "operator/schedules/form";
    }

    /** Show edit form */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return scheduleService.findById(id)
                .map(schedule -> {
                    model.addAttribute("schedule", schedule);
                    model.addAttribute("trains", trainService.findAll());
                    model.addAttribute("stations", stationService.findAll());
                    model.addAttribute("statuses", ScheduleStatus.values());
                    model.addAttribute("isEdit", true);
                    return "operator/schedules/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Schedule not found.");
                    return "redirect:/operator/schedules";
                });
    }

    /** Create or update schedule */
    @PostMapping
    public String save(@RequestParam Long trainId,
                       @RequestParam Long stationId,
                       @RequestParam(required = false) String arrivalTime,
                       @RequestParam(required = false) String departureTime,
                       @RequestParam(required = false) String scheduleDate,
                       @RequestParam String scheduleStatus,
                       @RequestParam(required = false) String dayOfWeek,
                       @RequestParam(required = false) Long scheduleId,
                       @RequestParam(required = false) Boolean isEdit,
                       RedirectAttributes redirectAttributes) {

        Schedule schedule;
        if (Boolean.TRUE.equals(isEdit) && scheduleId != null) {
            schedule = scheduleService.findById(scheduleId).orElse(new Schedule());
        } else {
            schedule = new Schedule();
        }

        // Set train
        trainService.findById(trainId).ifPresent(schedule::setTrain);
        // Set station
        stationService.findById(stationId).ifPresent(schedule::setStation);

        // Set times
        if (arrivalTime != null && !arrivalTime.isBlank()) {
            schedule.setArrivalTime(LocalTime.parse(arrivalTime));
        }
        if (departureTime != null && !departureTime.isBlank()) {
            schedule.setDepartureTime(LocalTime.parse(departureTime));
        }
        // Set date
        if (scheduleDate != null && !scheduleDate.isBlank()) {
            schedule.setScheduleDate(LocalDate.parse(scheduleDate));
        }

        schedule.setScheduleStatus(ScheduleStatus.valueOf(scheduleStatus));
        schedule.setDayOfWeek(dayOfWeek);

        Schedule saved = scheduleService.save(schedule);

        // Audit log
        String action = Boolean.TRUE.equals(isEdit) ? "UPDATE" : "CREATE";
        auditLogService.logAction(action, "Schedule", saved.getId(),
                action + " schedule for train " + saved.getTrain().getTrainNumber()
                        + " at " + saved.getStation().getName());

        redirectAttributes.addFlashAttribute("success",
                "Schedule " + (Boolean.TRUE.equals(isEdit) ? "updated" : "created") + " successfully.");
        return "redirect:/operator/schedules";
    }

    /** Delete schedule */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        scheduleService.findById(id).ifPresent(s -> {
            auditLogService.logAction("DELETE", "Schedule", id,
                    "DELETE schedule for train " + s.getTrain().getTrainNumber());
        });
        scheduleService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Schedule deleted.");
        return "redirect:/operator/schedules";
    }
}
