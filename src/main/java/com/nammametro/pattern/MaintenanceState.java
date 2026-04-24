package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

/**
 * Concrete state: MAINTENANCE.
 *
 * // Behavioral Pattern: State Pattern
 *
 * A train in maintenance can transition to: SCHEDULED (back in service).
 */
// Behavioral Pattern: State Pattern
public class MaintenanceState implements TrainState {

    @Override
    public TrainStatus getStatus() {
        return TrainStatus.MAINTENANCE;
    }

    @Override
    public boolean canTransitionTo(TrainStatus target) {
        return target == TrainStatus.SCHEDULED;
    }

    @Override
    public String getStateName() {
        return "Maintenance";
    }
}
