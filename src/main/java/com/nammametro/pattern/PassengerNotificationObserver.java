package com.nammametro.pattern;

import com.nammametro.model.Notification;
import com.nammametro.model.Ticket;
import com.nammametro.model.Train;
import com.nammametro.model.enums.NotificationType;
import com.nammametro.model.enums.TicketStatus;
import com.nammametro.repository.NotificationRepository;
import com.nammametro.repository.TicketRepository;
import com.nammametro.repository.ScheduleRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * // Behavioral Pattern: Observer Pattern
 *
 * Concrete observer that notifies passengers when their train
 * is DELAYED or CANCELLED. Finds all passengers with active tickets
 * (BOOKED or CONFIRMED) for the affected train's schedules and
 * creates a notification for each.
 *
 * SRP: This class has one responsibility — notifying affected passengers
 *      about train status changes.
 */
// Behavioral Pattern: Observer Pattern
@Component
public class PassengerNotificationObserver implements TrainStatusObserver {

    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;

    public PassengerNotificationObserver(TicketRepository ticketRepository,
                                          ScheduleRepository scheduleRepository,
                                          NotificationRepository notificationRepository) {
        this.ticketRepository = ticketRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void onStatusChange(Train train, String oldStatus, String newStatus) {
        // Only notify for DELAYED or CANCELLED
        if (!"DELAYED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
            return;
        }

        // Find all schedules for this train
        var schedules = scheduleRepository.findByTrainId(train.getId());

        // Collect all passengers with active tickets on those schedules
        Set<Long> notifiedUserIds = new HashSet<>();

        for (var schedule : schedules) {
            // Find tickets for this schedule's train
            List<Ticket> tickets = ticketRepository.findByPassengerId(null); // fallback
            // Use broader query: all tickets matching the train
            tickets = ticketRepository.findAll().stream()
                    .filter(t -> t.getSchedule() != null
                            && t.getSchedule().getTrain() != null
                            && t.getSchedule().getTrain().getId().equals(train.getId()))
                    .filter(t -> t.getStatus() == TicketStatus.BOOKED
                            || t.getStatus() == TicketStatus.CONFIRMED)
                    .toList();

            for (Ticket ticket : tickets) {
                Long userId = ticket.getPassenger().getUser().getId();
                if (notifiedUserIds.contains(userId)) {
                    continue; // avoid duplicate notifications
                }
                notifiedUserIds.add(userId);

                Notification notif = new Notification();
                notif.setUser(ticket.getPassenger().getUser());

                if ("DELAYED".equals(newStatus)) {
                    notif.setTitle("⚠️ Train Delayed — " + train.getTrainNumber());
                    notif.setMessage("Train " + train.getTrainNumber()
                            + (train.getName() != null ? " (" + train.getName() + ")" : "")
                            + " has been delayed. Your ticket #" + ticket.getId()
                            + " may be affected. Please check the schedule.");
                    notif.setNotificationType(NotificationType.DELAY);
                } else {
                    notif.setTitle("❌ Train Cancelled — " + train.getTrainNumber());
                    notif.setMessage("Train " + train.getTrainNumber()
                            + (train.getName() != null ? " (" + train.getName() + ")" : "")
                            + " has been cancelled. Your ticket #" + ticket.getId()
                            + " will be auto-cancelled with a refund.");
                    notif.setNotificationType(NotificationType.CANCELLATION);
                }

                notif.setIsRead(false);
                notificationRepository.save(notif);
            }
            break; // only process once (all tickets found above)
        }
    }
}
