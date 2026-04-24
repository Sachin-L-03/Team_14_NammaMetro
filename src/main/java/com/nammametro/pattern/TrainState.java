package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

// Behavioral Pattern: State Pattern
public interface TrainState {

    /**
     * Returns the TrainStatus that this state represents.
     */
    TrainStatus getStatus();

    /**
     * Determines whether a transition to the target status is allowed.
     *
     * @param target the desired next status
     * @return true if the transition is valid
     */
    boolean canTransitionTo(TrainStatus target);

    /**
     * Returns a human-readable name of this state.
     */
    String getStateName();
}
