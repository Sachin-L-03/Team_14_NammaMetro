package com.nammametro.controller;

import com.nammametro.model.Admin;
import com.nammametro.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests related to Admin operations.
 *
 * SRP: This class has one responsibility — routing admin-related web requests.
 */
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String listAdmins(Model model) {
        model.addAttribute("admins", adminService.findAll());
        return "admins/list";
    }

    @GetMapping("/{id}")
    public String viewAdmin(@PathVariable Long id, Model model) {
        adminService.findById(id).ifPresent(admin -> model.addAttribute("admin", admin));
        return "admins/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admins/form";
    }

    @PostMapping
    public String saveAdmin(@ModelAttribute Admin admin) {
        adminService.save(admin);
        return "redirect:/admins";
    }

    @GetMapping("/{id}/delete")
    public String deleteAdmin(@PathVariable Long id) {
        adminService.deleteById(id);
        return "redirect:/admins";
    }
}
