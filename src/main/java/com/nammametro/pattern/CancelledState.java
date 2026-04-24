package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

/**
 * Concrete state: CANCELLED.
 *
 * // Behavioral Pattern: State Pattern
 *
 * A cancelled train can transition to: SCHEDULED (rescheduled), MAINTENANCE.
 */
// Behavioral Pattern: State Pattern
public class CancelledState implements TrainState {

    @Override
    public TrainStatus getStatus() {
        return TrainStatus.CANCELLED;
    }

    @Override
    public boolean canTransitionTo(TrainStatus target) {
        return switch (target) {
            case SCHEDULED, MAINTENANCE -> true;
            default -> false;
        };
    }

    @Override
    public String getStateName() {
        return "Cancelled";
    }
}
