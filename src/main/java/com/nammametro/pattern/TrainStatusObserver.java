package com.nammametro.pattern;

import com.nammametro.model.Train;

/**
 * ============================================================
 *  DESIGN PATTERN: Observer Pattern
 * ============================================================
 *
 *  // Behavioral Pattern: Observer Pattern
 *
 *  Pattern Purpose:
 *      The Observer Pattern defines a one-to-many dependency between
 *      objects so that when one object (the Subject/Publisher) changes
 *      state, all its dependents (Observers) are notified automatically.
 *
 *  Why Observer for Train status changes?
 *      When an operator changes a train's status, multiple systems must
 *      react: passengers need notifications, incidents need logging,
 *      and potentially more actions in the future. The Observer Pattern
 *      decouples the status change from its reactions — new observers
 *      can be added without modifying the publisher.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for observing train status changes.
 * ============================================================
 */
// Behavioral Pattern: Observer Pattern
public interface TrainStatusObserver {

    /**
     * Called when a train's status changes.
     *
     * @param train     the train whose status changed
     * @param oldStatus the previous status string
     * @param newStatus the new status string
     */
    void onStatusChange(Train train, String oldStatus, String newStatus);
}
