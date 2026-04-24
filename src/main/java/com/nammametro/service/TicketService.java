package com.nammametro.service;

import com.nammametro.model.Ticket;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic layer for Ticket operations including booking,
 * cancellation, and refund logic.
 *
 * SRP: This class has one responsibility — encapsulating business rules for tickets.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> findByPassengerId(Long passengerId) {
        return ticketRepository.findByPassengerId(passengerId);
    }

    /** Paginated booking history, most recent first */
    public Page<Ticket> findByPassengerIdPaginated(Long passengerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ticketRepository.findByPassengerIdOrderByBookingTimeDesc(passengerId, pageable);
    }

    /** Recent 5 bookings for dashboard display */
    public List<Ticket> findRecentByPassengerId(Long passengerId) {
        return ticketRepository.findTop5ByPassengerIdOrderByBookingTimeDesc(passengerId);
    }

    public List<Ticket> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    /**
     * Generates a unique QR code string for a ticket.
     * Format: NM-{UUID}-{timestamp}
     */
    public String generateQrCode() {
        return "NM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + System.currentTimeMillis();
    }

    /**
     * Ticket state machine: checks if a transition is valid.
     *
     * Valid transitions:
     *   BOOKED → CONFIRMED, CANCELLED, EXPIRED
     *   CONFIRMED → USED, CANCELLED, EXPIRED
     *   USED → (terminal)
     *   CANCELLED → (terminal)
     *   EXPIRED → (terminal)
     */
    public boolean canTransition(TicketStatus current, TicketStatus target) {
        return switch (current) {
            case BOOKED -> target == TicketStatus.CONFIRMED
                    || target == TicketStatus.CANCELLED
                    || target == TicketStatus.EXPIRED;
            case CONFIRMED -> target == TicketStatus.USED
                    || target == TicketStatus.CANCELLED
                    || target == TicketStatus.EXPIRED;
            default -> false; // USED, CANCELLED, EXPIRED are terminal
        };
    }

    /**
     * Calculates refund amount for a cancelled ticket.
     *
     * Refund policy:
     *   - 100% if > 1 hour before departure
     *   - 50% otherwise
     *
     * @param ticket the ticket being cancelled
     * @return the refund amount
     */
    public BigDecimal calculateRefund(Ticket ticket) {
        LocalTime departureTime = ticket.getSchedule().getDepartureTime();
        if (departureTime == null) {
            // If no departure time, give 50% refund
            return ticket.getFare().multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureDateTime;

        if (ticket.getSchedule().getScheduleDate() != null) {
            departureDateTime = ticket.getSchedule().getScheduleDate().atTime(departureTime);
        } else {
            // Fallback: use today's date
            departureDateTime = now.toLocalDate().atTime(departureTime);
        }

        // More than 1 hour before departure → 100% refund
        if (now.plusHours(1).isBefore(departureDateTime)) {
            return ticket.getFare();
        }
        // Otherwise → 50% refund
        return ticket.getFare().multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
