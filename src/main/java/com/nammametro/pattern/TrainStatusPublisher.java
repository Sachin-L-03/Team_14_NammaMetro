package com.nammametro.pattern;

import com.nammametro.model.Train;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
// Behavioral Pattern: Observer Pattern
@Component
public class TrainStatusPublisher {

    private final List<TrainStatusObserver> observers;

    /**
     * Spring auto-injects all TrainStatusObserver beans.
     * New observers are automatically registered.
     */
    public TrainStatusPublisher(List<TrainStatusObserver> observers) {
        this.observers = observers != null ? observers : new ArrayList<>();
    }

    /**
     * Registers an observer at runtime.
     */
    public void addObserver(TrainStatusObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer at runtime.
     */
    public void removeObserver(TrainStatusObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers about a train status change.
     *
     * @param train     the train whose status changed
     * @param oldStatus the previous status
     * @param newStatus the new status
     */
    public void notifyObservers(Train train, String oldStatus, String newStatus) {
        for (TrainStatusObserver observer : observers) {
            observer.onStatusChange(train, oldStatus, newStatus);
        }
    }
}
