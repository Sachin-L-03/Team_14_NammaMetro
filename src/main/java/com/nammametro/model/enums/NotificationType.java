package com.nammametro.model.enums;

/**
 * Types of notifications sent to passengers.
 *
 * SRP: This enum has one responsibility — defining the set of valid notification types.
 */
public enum NotificationType {
    DELAY,
    CANCELLATION,
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    TICKET_EXPIRED,
    GENERAL
}
