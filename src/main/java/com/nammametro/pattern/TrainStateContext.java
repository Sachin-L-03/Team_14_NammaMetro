package com.nammametro.pattern;

import com.nammametro.model.enums.TrainStatus;

/**
 * Context class for the State Pattern.
 *
 * // Behavioral Pattern: State Pattern
 *
 * Wraps the current TrainState and delegates transition logic to it.
 * The operator calls canTransitionTo() to check validity, then
 * transitionTo() to perform the state change.
 *
 * SRP: This class has one responsibility — managing train state transitions
 *      by delegating to the current concrete state object.
 */
// Behavioral Pattern: State Pattern
public class TrainStateContext {

    private TrainState currentState;

    /**
     * Creates a context from an existing TrainStatus.
     */
    public TrainStateContext(TrainStatus status) {
        this.currentState = resolveState(status);
    }

    /**
     * Returns the current state object.
     */
    public TrainState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the current TrainStatus enum value.
     */
    public TrainStatus getCurrentStatus() {
        return currentState.getStatus();
    }

    /**
     * Checks whether the transition to the target status is allowed
     * according to the current state's rules.
     */
    public boolean canTransitionTo(TrainStatus target) {
        return currentState.canTransitionTo(target);
    }

    /**
     * Transitions to the target status if allowed.
     *
     * @param target the desired next status
     * @return true if the transition succeeded
     * @throws IllegalStateException if the transition is not allowed
     */
    public boolean transitionTo(TrainStatus target) {
        if (!currentState.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Invalid state transition: " + currentState.getStateName()
                    + " → " + target.name()
                    + ". This transition is not allowed.");
        }
        this.currentState = resolveState(target);
        return true;
    }

    /**
     * Maps a TrainStatus enum to its corresponding concrete TrainState object.
     */
    private TrainState resolveState(TrainStatus status) {
        return switch (status) {
            case RUNNING     -> new RunningState();
            case DELAYED     -> new DelayedState();
            case CANCELLED   -> new CancelledState();
            case MAINTENANCE -> new MaintenanceState();
            // SCHEDULED and COMPLETED use a permissive default state
            default -> new ScheduledOrCompletedState(status);
        };
    }

    /**
     * Fallback state for SCHEDULED and COMPLETED statuses.
     * SCHEDULED can transition to RUNNING, CANCELLED, MAINTENANCE.
     * COMPLETED is terminal — no transitions allowed.
     */
    // Behavioral Pattern: State Pattern
    private static class ScheduledOrCompletedState implements TrainState {
        private final TrainStatus status;

        ScheduledOrCompletedState(TrainStatus status) {
            this.status = status;
        }

        @Override
        public TrainStatus getStatus() {
            return status;
        }

        @Override
        public boolean canTransitionTo(TrainStatus target) {
            if (status == TrainStatus.SCHEDULED) {
                return target == TrainStatus.RUNNING
                    || target == TrainStatus.CANCELLED
                    || target == TrainStatus.MAINTENANCE;
            }
            // COMPLETED — terminal state
            return false;
        }

        @Override
        public String getStateName() {
            return status.name().charAt(0)
                    + status.name().substring(1).toLowerCase();
        }
    }
}
