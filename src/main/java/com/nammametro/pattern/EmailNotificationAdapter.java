package com.nammametro.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * // Structural Pattern: Adapter Pattern
 *
 * Adapter that sends notifications via Email.
 * Internally adapts the email-sending mechanism to the
 * NotificationSender interface.
 *
 * In a production system, this would integrate with an SMTP
 * server or email API (SendGrid, SES, etc.).
 *
 * SRP: This class has one responsibility — adapting email sending
 *      to the NotificationSender interface.
 */
// Structural Pattern: Adapter Pattern
@Component
public class EmailNotificationAdapter implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public boolean send(String recipient, String subject, String message) {
        // Adapter: translates the generic send() call into email-specific logic
        log.info("[EMAIL] Sending email to: {} | Subject: {} | Body: {}",
                recipient, subject, message);

        // In production: use JavaMailSender or external API
        // mailSender.send(createMimeMessage(recipient, subject, message));

        return true; // simulated success
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
