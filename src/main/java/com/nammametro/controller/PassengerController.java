package com.nammametro.controller;

import com.nammametro.model.Passenger;
import com.nammametro.service.PassengerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests related to Passenger operations.
 *
 * SRP: This class has one responsibility — routing passenger-related web requests.
 */
@Controller
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping
    public String listPassengers(Model model) {
        model.addAttribute("passengers", passengerService.findAll());
        return "passengers/list";
    }

    @GetMapping("/{id}")
    public String viewPassenger(@PathVariable Long id, Model model) {
        passengerService.findById(id).ifPresent(p -> model.addAttribute("passenger", p));
        return "passengers/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("passenger", new Passenger());
        return "passengers/form";
    }

    @PostMapping
    public String savePassenger(@ModelAttribute Passenger passenger) {
        passengerService.save(passenger);
        return "redirect:/passengers";
    }

    @GetMapping("/{id}/delete")
    public String deletePassenger(@PathVariable Long id) {
        passengerService.deleteById(id);
        return "redirect:/passengers";
    }
}
