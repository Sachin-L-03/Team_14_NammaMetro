package com.nammametro.pattern;

/**
 * ============================================================
 *  DESIGN PATTERN: Adapter Pattern
 * ============================================================
 *
 *  // Structural Pattern: Adapter Pattern
 *
 *  Pattern Purpose:
 *      The Adapter Pattern allows objects with incompatible interfaces
 *      to collaborate. It wraps an existing class with a new interface
 *      so that it becomes compatible with the client's expected interface.
 *
 *  Why Adapter for Notifications?
 *      The notification system needs to send messages through different
 *      channels (Email, SMS, App push). Each channel has a different
 *      internal implementation, but the NotificationService should not
 *      care which channel is used — it just calls send().
 *
 *      This also implements the Dependency Inversion Principle (DIP):
 *      the high-level NotificationService depends on this abstraction,
 *      not on concrete implementations.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for sending a notification message.
 * ============================================================
 */
// Structural Pattern: Adapter Pattern
public interface NotificationSender {

    /**
     * Sends a notification message to the recipient.
     *
     * @param recipient the target (email, phone, userId, etc.)
     * @param subject   the notification title/subject
     * @param message   the notification body
     * @return true if the message was sent successfully
     */
    boolean send(String recipient, String subject, String message);

    /**
     * Returns the channel name (e.g., "EMAIL", "SMS", "APP").
     */
    String getChannelName();
}
