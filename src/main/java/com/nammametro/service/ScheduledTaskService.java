package com.nammametro.service;

import com.nammametro.model.Notification;
import com.nammametro.model.Ticket;
import com.nammametro.model.enums.NotificationType;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.repository.NotificationRepository;
import com.nammametro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Background scheduled tasks for ticket lifecycle management.
 *
 * Runs periodically to:
 *   1. Expire tickets where journey time has passed
 *   2. Auto-cancel BOOKED tickets older than 10 minutes (unpaid)
 *
 * SRP: This class has one responsibility — executing scheduled
 *      ticket lifecycle transitions.
 */
@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;

    public ScheduledTaskService(TicketRepository ticketRepository,
                                 NotificationRepository notificationRepository) {
        this.ticketRepository = ticketRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Runs every 5 minutes.
     *
     * Task 1: Scan BOOKED/CONFIRMED tickets where the scheduled journey
     *         time has passed → set status to EXPIRED.
     *
     * Task 2: Scan BOOKED tickets older than 10 minutes → auto-cancel
     *         (simulates unpaid booking timeout).
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 ms
    public void processTicketLifecycle() {
        log.info("[SCHEDULED] Running ticket lifecycle check...");

        expirePassedTickets();
        autoCancelStaleBookings();

        log.info("[SCHEDULED] Ticket lifecycle check complete.");
    }

    /**
     * Expires BOOKED/CONFIRMED tickets where the journey departure
     * time has already passed.
     */
    private void expirePassedTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.BOOKED
                        || t.getStatus() == TicketStatus.CONFIRMED)
                .filter(t -> {
                    // Check if departure time has passed
                    if (t.getSchedule() == null || t.getSchedule().getDepartureTime() == null) {
                        return false;
                    }
                    LocalDate scheduleDate = t.getSchedule().getScheduleDate() != null
                            ? t.getSchedule().getScheduleDate()
                            : now.toLocalDate();
                    LocalTime depTime = t.getSchedule().getDepartureTime();
                    LocalDateTime departure = scheduleDate.atTime(depTime);

                    return now.isAfter(departure);
                })
                .toList();

        for (Ticket ticket : activeTickets) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);

            // Notify passenger
            Notification notif = new Notification();
            notif.setUser(ticket.getPassenger().getUser());
            notif.setTitle("⏰ Ticket Expired — #" + ticket.getId());
            notif.setMessage("Your ticket #" + ticket.getId()
                    + " (" + ticket.getSourceStation().getName()
                    + " → " + ticket.getDestStation().getName()
                    + ") has expired as the journey time has passed.");
            notif.setNotificationType(NotificationType.TICKET_EXPIRED);
            notif.setIsRead(false);
            notificationRepository.save(notif);

            log.info("[SCHEDULED] Expired ticket #{}", ticket.getId());
        }

        if (!activeTickets.isEmpty()) {
            log.info("[SCHEDULED] Expired {} tickets.", activeTickets.size());
        }
    }

    /**
     * Auto-cancels BOOKED tickets that are older than 10 minutes
     * (simulating payment timeout).
     */
    private void autoCancelStaleBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Ticket> staleBookings = ticketRepository.findByStatus(TicketStatus.BOOKED).stream()
                .filter(t -> t.getBookingTime() != null && t.getBookingTime().isBefore(cutoff))
                .toList();

        for (Ticket ticket : staleBookings) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setRefundAmount(ticket.getFare()); // full refund for auto-cancel
            ticketRepository.save(ticket);

            // Notify passenger
            Notification notif = new Notification();
            notif.setUser(ticket.getPassenger().getUser());
            notif.setTitle("⚠️ Booking Auto-Cancelled — #" + ticket.getId());
            notif.setMessage("Your ticket #" + ticket.getId()
                    + " was automatically cancelled because it was not confirmed "
                    + "within 10 minutes. Full refund of ₹" + ticket.getFare()
                    + " will be processed.");
            notif.setNotificationType(NotificationType.BOOKING_CANCELLED);
            notif.setIsRead(false);
            notificationRepository.save(notif);

            log.info("[SCHEDULED] Auto-cancelled stale booking #{}", ticket.getId());
        }

        if (!staleBookings.isEmpty()) {
            log.info("[SCHEDULED] Auto-cancelled {} stale bookings.", staleBookings.size());
        }
    }
}
