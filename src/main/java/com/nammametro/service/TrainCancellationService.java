package com.nammametro.service;

import com.nammametro.model.Notification;
import com.nammametro.model.Ticket;
import com.nammametro.model.Train;
import com.nammametro.model.enums.NotificationType;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.repository.NotificationRepository;
import com.nammametro.repository.ScheduleRepository;
import com.nammametro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Handles the cascading effects of a train cancellation:
 * auto-cancels all CONFIRMED/BOOKED tickets for the train's schedules
 * and sends cancellation notifications.
 *
 * SRP: This class has one responsibility — managing ticket auto-cancellation
 *      when a train is cancelled.
 */
@Service
public class TrainCancellationService {

    private static final Logger log = LoggerFactory.getLogger(TrainCancellationService.class);

    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;

    public TrainCancellationService(TicketRepository ticketRepository,
                                     ScheduleRepository scheduleRepository,
                                     NotificationRepository notificationRepository) {
        this.ticketRepository = ticketRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Auto-cancels all active tickets (BOOKED + CONFIRMED) for a cancelled train.
     * Applies full refund (train cancelled by operator = 100% refund).
     * Sends a notification to each affected passenger.
     */
    public int autoCancelTicketsForTrain(Train train) {
        log.info("Auto-cancelling tickets for cancelled train: {}", train.getTrainNumber());

        // Find all tickets linked to this train's schedules
        List<Ticket> affectedTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getSchedule() != null
                        && t.getSchedule().getTrain() != null
                        && t.getSchedule().getTrain().getId().equals(train.getId()))
                .filter(t -> t.getStatus() == TicketStatus.BOOKED
                        || t.getStatus() == TicketStatus.CONFIRMED)
                .toList();

        int cancelledCount = 0;
        for (Ticket ticket : affectedTickets) {
            // Full refund for operator-cancelled train
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setRefundAmount(ticket.getFare());
            ticketRepository.save(ticket);

            // Send cancellation notification
            Notification notif = new Notification();
            notif.setUser(ticket.getPassenger().getUser());
            notif.setTitle("❌ Ticket Auto-Cancelled — Train " + train.getTrainNumber());
            notif.setMessage("Your ticket #" + ticket.getId()
                    + " (" + ticket.getSourceStation().getName()
                    + " → " + ticket.getDestStation().getName()
                    + ") has been automatically cancelled because train "
                    + train.getTrainNumber() + " was cancelled by the operator. "
                    + "Full refund of ₹" + ticket.getFare() + " will be processed.");
            notif.setNotificationType(NotificationType.CANCELLATION);
            notif.setIsRead(false);
            notificationRepository.save(notif);

            cancelledCount++;
            log.info("Auto-cancelled ticket #{} for passenger {}",
                    ticket.getId(), ticket.getPassenger().getUser().getName());
        }

        log.info("Auto-cancelled {} tickets for train {}", cancelledCount, train.getTrainNumber());
        return cancelledCount;
    }
}
