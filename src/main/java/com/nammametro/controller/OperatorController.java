package com.nammametro.controller;

import com.nammametro.model.Operator;
import com.nammametro.service.OperatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests related to Operator operations.
 *
 * SRP: This class has one responsibility — routing operator-related web requests.
 */
@Controller
@RequestMapping("/operators")
public class OperatorController {

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    public String listOperators(Model model) {
        model.addAttribute("operators", operatorService.findAll());
        return "operators/list";
    }

    @GetMapping("/{id}")
    public String viewOperator(@PathVariable Long id, Model model) {
        operatorService.findById(id).ifPresent(op -> model.addAttribute("operator", op));
        return "operators/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("operator", new Operator());
        return "operators/form";
    }

    @PostMapping
    public String saveOperator(@ModelAttribute Operator operator) {
        operatorService.save(operator);
        return "redirect:/operators";
    }

    @GetMapping("/{id}/delete")
    public String deleteOperator(@PathVariable Long id) {
        operatorService.deleteById(id);
        return "redirect:/operators";
    }
}
