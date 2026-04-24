package com.nammametro.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * // Structural Pattern: Adapter Pattern
 *
 * Adapter that sends notifications via SMS.
 * Internally adapts the SMS-sending mechanism to the
 * NotificationSender interface.
 *
 * In a production system, this would integrate with an SMS
 * gateway (Twilio, AWS SNS, etc.).
 *
 * SRP: This class has one responsibility — adapting SMS sending
 *      to the NotificationSender interface.
 */
// Structural Pattern: Adapter Pattern
@Component
public class SMSNotificationAdapter implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SMSNotificationAdapter.class);

    @Override
    public boolean send(String recipient, String subject, String message) {
        // Adapter: translates the generic send() call into SMS-specific logic
        log.info("[SMS] Sending SMS to: {} | Message: {} — {}",
                recipient, subject, message);

        // In production: use Twilio client
        // twilioClient.messages().create(new PhoneNumber(recipient), ..., message);

        return true; // simulated success
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
