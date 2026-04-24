package com.nammametro.service;

import com.nammametro.model.Notification;
import com.nammametro.model.User;
import com.nammametro.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic layer for Notification operations.
 *
 * SRP: This class has one responsibility — encapsulating business rules for notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findRecentByUserId(Long userId) {
        return notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Creates and saves a notification for a user (defaults to GENERAL type).
     */
    public Notification createNotification(User user, String title, String message) {
        return createNotification(user, title, message,
                com.nammametro.model.enums.NotificationType.GENERAL);
    }

    /**
     * Creates and saves a typed notification for a user.
     */
    public Notification createNotification(User user, String title, String message,
                                            com.nammametro.model.enums.NotificationType type) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setNotificationType(type);
        notif.setIsRead(false);
        return notificationRepository.save(notif);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public java.util.Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }
}
