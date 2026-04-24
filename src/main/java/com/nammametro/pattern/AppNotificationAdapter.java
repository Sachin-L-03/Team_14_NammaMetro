package com.nammametro.pattern;

import com.nammametro.model.Notification;
import com.nammametro.model.User;
import com.nammametro.model.enums.NotificationType;
import com.nammametro.repository.NotificationRepository;
import com.nammametro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Structural Pattern: Adapter Pattern
@Component
public class AppNotificationAdapter implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(AppNotificationAdapter.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public AppNotificationAdapter(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean send(String recipient, String subject, String message) {
        // Adapter: translates the generic send() call into DB persistence
        log.info("[APP] Creating in-app notification for user: {} | Title: {}",
                recipient, subject);

        try {
            // recipient is expected to be a user email
            var userOpt = userRepository.findByEmail(recipient);
            if (userOpt.isPresent()) {
                Notification notif = new Notification();
                notif.setUser(userOpt.get());
                notif.setTitle(subject);
                notif.setMessage(message);
                notif.setNotificationType(NotificationType.GENERAL);
                notif.setIsRead(false);
                notificationRepository.save(notif);
                return true;
            }
            log.warn("[APP] User not found for email: {}", recipient);
            return false;
        } catch (Exception e) {
            log.error("[APP] Failed to create notification for {}: {}", recipient, e.getMessage());
            return false;
        }
    }

    @Override
    public String getChannelName() {
        return "APP";
    }
}
