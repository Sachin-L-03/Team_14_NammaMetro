package com.nammametro.model.enums;

/**
 * Possible statuses for a metro ticket.
 *
 * SRP: This enum has one responsibility — defining the set of valid ticket statuses.
 */
public enum TicketStatus {
    BOOKED,
    CONFIRMED,
    CANCELLED,
    USED,
    EXPIRED
}
