package com.nammametro.model.enums;

/**
 * Possible statuses for a metro train.
 *
 * SRP: This enum has one responsibility — defining the set of valid train statuses.
 */
public enum TrainStatus {
    SCHEDULED,
    RUNNING,
    DELAYED,
    CANCELLED,
    COMPLETED,
    MAINTENANCE
}
