package com.nammametro.controller;

import com.nammametro.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin system logs viewer — paginated, filterable by action type.
 *
 * SRP: This class has one responsibility — handling admin log viewing requests.
 */
@Controller
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminService adminService;

    public AdminLogController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String viewLogs(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String action,
                            Model model) {

        var logsPage = adminService.getLogsPage(page, 20, action);

        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("totalItems", logsPage.getTotalElements());
        model.addAttribute("actions", adminService.getDistinctActions());
        model.addAttribute("selectedAction", action);

        return "admin/logs";
    }
}
