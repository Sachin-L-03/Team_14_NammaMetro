package com.nammametro.controller;

import com.nammametro.model.*;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.service.*;
import com.nammametro.service.fare.FareBreakdown;
import com.nammametro.service.fare.FareCalculator;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles ticket booking, cancellation, and booking history for passengers.
 *
 * SRP: This class has one responsibility — handling passenger ticketing operations.
 */
@Controller
@RequestMapping("/passenger/tickets")
public class PassengerTicketController {

    private final TicketService ticketService;
    private final ScheduleService scheduleService;
    private final StationService stationService;
    private final UserService userService;
    private final PassengerService passengerService;
    private final FareCalculator fareCalculator;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final QrCodeService qrCodeService;

    public PassengerTicketController(TicketService ticketService,
                                      ScheduleService scheduleService,
                                      StationService stationService,
                                      UserService userService,
                                      PassengerService passengerService,
                                      FareCalculator fareCalculator,
                                      NotificationService notificationService,
                                      AuditLogService auditLogService,
                                      QrCodeService qrCodeService) {
        this.ticketService = ticketService;
        this.scheduleService = scheduleService;
        this.stationService = stationService;
        this.userService = userService;
        this.passengerService = passengerService;
        this.fareCalculator = fareCalculator;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Book a ticket: passenger selects schedule + source/dest stations.
     */
    @PostMapping("/book")
    public String bookTicket(@RequestParam Long scheduleId,
                              @RequestParam Long sourceStationId,
                              @RequestParam Long destStationId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        // Resolve passenger
        Passenger passenger = resolvePassenger(authentication);
        if (passenger == null) {
            redirectAttributes.addFlashAttribute("error", "Passenger profile not found.");
            return "redirect:/passenger/dashboard";
        }

        // Resolve entities
        Schedule schedule = scheduleService.findById(scheduleId).orElse(null);
        Station source = stationService.findById(sourceStationId).orElse(null);
        Station dest = stationService.findById(destStationId).orElse(null);

        if (schedule == null || source == null || dest == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid booking details.");
            return "redirect:/passenger/search";
        }

        // Calculate fare
        BigDecimal distance = BigDecimal.ZERO;
        if (schedule.getTrain() != null && schedule.getTrain().getRoute() != null
                && schedule.getTrain().getRoute().getDistanceKm() != null) {
            distance = schedule.getTrain().getRoute().getDistanceKm();
        } else {
            distance = new BigDecimal("5"); // default
        }

        boolean hasMetroCard = Boolean.TRUE.equals(passenger.getHasMetroCard());
        FareBreakdown breakdown = fareCalculator.calculate(distance, hasMetroCard);

        // Create ticket
        Ticket ticket = new Ticket();
        ticket.setPassenger(passenger);
        ticket.setSchedule(schedule);
        ticket.setSourceStation(source);
        ticket.setDestStation(dest);
        ticket.setFare(breakdown.getFinalFare());
        ticket.setStatus(TicketStatus.BOOKED);
        ticket.setBookingTime(LocalDateTime.now());
        ticket.setQrCode(ticketService.generateQrCode());

        Ticket saved = ticketService.save(ticket);

        // Create notification
        notificationService.createNotification(
                passenger.getUser(),
                "Ticket Booked 🎫",
                "Ticket #" + saved.getId() + " booked for "
                        + source.getName() + " → " + dest.getName()
                        + " | Fare: ₹" + saved.getFare()
        );

        // Audit log
        auditLogService.logAction("BOOK", "Ticket", saved.getId(),
                "Booked ticket: " + source.getName() + " → " + dest.getName()
                        + " | ₹" + saved.getFare());

        redirectAttributes.addFlashAttribute("success",
                "Ticket booked successfully! QR: " + saved.getQrCode());
        return "redirect:/passenger/tickets/" + saved.getId();
    }

    /**
     * Confirm a booked ticket (simulates payment).
     */
    @PostMapping("/{id}/confirm")
    public String confirmTicket(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        return ticketService.findById(id)
                .map(ticket -> {
                    if (!ticketService.canTransition(ticket.getStatus(), TicketStatus.CONFIRMED)) {
                        redirectAttributes.addFlashAttribute("error",
                                "Cannot confirm ticket in " + ticket.getStatus() + " status.");
                        return "redirect:/passenger/tickets/" + id;
                    }
                    ticket.setStatus(TicketStatus.CONFIRMED);
                    ticketService.save(ticket);

                    notificationService.createNotification(
                            ticket.getPassenger().getUser(),
                            "Ticket Confirmed ✅",
                            "Ticket #" + id + " has been confirmed."
                    );

                    redirectAttributes.addFlashAttribute("success", "Ticket confirmed!");
                    return "redirect:/passenger/tickets/" + id;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Ticket not found.");
                    return "redirect:/passenger/tickets/history";
                });
    }

    /**
     * Cancel a ticket with refund calculation.
     */
    @PostMapping("/{id}/cancel")
    public String cancelTicket(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        return ticketService.findById(id)
                .map(ticket -> {
                    if (!ticketService.canTransition(ticket.getStatus(), TicketStatus.CANCELLED)) {
                        redirectAttributes.addFlashAttribute("error",
                                "Cannot cancel ticket in " + ticket.getStatus() + " status.");
                        return "redirect:/passenger/tickets/" + id;
                    }

                    BigDecimal refund = ticketService.calculateRefund(ticket);
                    ticket.setStatus(TicketStatus.CANCELLED);
                    ticket.setRefundAmount(refund);
                    ticketService.save(ticket);

                    notificationService.createNotification(
                            ticket.getPassenger().getUser(),
                            "Ticket Cancelled ❌",
                            "Ticket #" + id + " cancelled. Refund: ₹" + refund
                    );

                    auditLogService.logAction("CANCEL", "Ticket", id,
                            "Cancelled ticket. Refund: ₹" + refund);

                    redirectAttributes.addFlashAttribute("success",
                            "Ticket cancelled. Refund: ₹" + refund);
                    return "redirect:/passenger/tickets/" + id;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Ticket not found.");
                    return "redirect:/passenger/tickets/history";
                });
    }

    /**
     * Ticket detail page.
     */
    @GetMapping("/{id}")
    public String ticketDetail(@PathVariable Long id, Model model,
                                RedirectAttributes redirectAttributes) {
        return ticketService.findById(id)
                .map(ticket -> {
                    model.addAttribute("ticket", ticket);
                    model.addAttribute("canConfirm",
                            ticketService.canTransition(ticket.getStatus(), TicketStatus.CONFIRMED));
                    model.addAttribute("canCancel",
                            ticketService.canTransition(ticket.getStatus(), TicketStatus.CANCELLED));

                    // Generate QR code image (Base64) using ZXing
                    try {
                        String qrContent = qrCodeService.createTicketQrContent(
                                ticket.getId(),
                                ticket.getPassenger().getId(),
                                ticket.getSchedule().getId());
                        String qrBase64 = qrCodeService.generateQrCodeBase64(qrContent);
                        model.addAttribute("qrCodeImage", qrBase64);
                    } catch (Exception e) {
                        // Fallback: show QR code string
                        model.addAttribute("qrCodeImage", null);
                    }

                    return "passenger/ticket-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Ticket not found.");
                    return "redirect:/passenger/tickets/history";
                });
    }

    /**
     * Paginated booking history.
     */
    @GetMapping("/history")
    public String bookingHistory(@RequestParam(defaultValue = "0") int page,
                                  Authentication authentication,
                                  Model model) {
        Passenger passenger = resolvePassenger(authentication);
        if (passenger != null) {
            Page<Ticket> ticketPage = ticketService.findByPassengerIdPaginated(
                    passenger.getId(), page, 10);
            model.addAttribute("tickets", ticketPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ticketPage.getTotalPages());
            model.addAttribute("totalItems", ticketPage.getTotalElements());
        }
        return "passenger/history";
    }

    /**
     * Resolves the currently authenticated passenger.
     */
    private Passenger resolvePassenger(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            return userService.findByEmail(ud.getUsername())
                    .flatMap(user -> passengerService.findByUserId(user.getId()))
                    .orElse(null);
        }
        return null;
    }
}
