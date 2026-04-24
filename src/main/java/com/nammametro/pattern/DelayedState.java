package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

/**
 * Concrete state: DELAYED.
 *
 * // Behavioral Pattern: State Pattern
 *
 * A delayed train can transition to: RUNNING (resumed), CANCELLED, MAINTENANCE.
 */
// Behavioral Pattern: State Pattern
public class DelayedState implements TrainState {

    @Override
    public TrainStatus getStatus() {
        return TrainStatus.DELAYED;
    }

    @Override
    public boolean canTransitionTo(TrainStatus target) {
        return switch (target) {
            case RUNNING, CANCELLED, MAINTENANCE -> true;
            default -> false;
        };
    }

    @Override
    public String getStateName() {
        return "Delayed";
    }
}
