package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

/**
 * Concrete state: RUNNING.
 *
 * // Behavioral Pattern: State Pattern
 *
 * A running train can transition to: DELAYED, CANCELLED, COMPLETED, MAINTENANCE.
 * It cannot go back to SCHEDULED.
 */
// Behavioral Pattern: State Pattern
public class RunningState implements TrainState {

    @Override
    public TrainStatus getStatus() {
        return TrainStatus.RUNNING;
    }

    @Override
    public boolean canTransitionTo(TrainStatus target) {
        return switch (target) {
            case DELAYED, CANCELLED, COMPLETED, MAINTENANCE -> true;
            default -> false;
        };
    }

    @Override
    public String getStateName() {
        return "Running";
    }
}
