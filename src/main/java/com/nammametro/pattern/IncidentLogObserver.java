package com.nammametro.pattern;

import com.nammametro.model.Log;
import com.nammametro.model.Train;
import com.nammametro.repository.LogRepository;
import org.springframework.stereotype.Component;

/**
 * // Behavioral Pattern: Observer Pattern
 *
 * Concrete observer that logs every train status change to the logs table.
 * Acts as an audit trail independent of the AuditLogService — this observer
 * is triggered automatically via the Observer Pattern.
 *
 * SRP: This class has one responsibility — logging train status changes.
 */
// Behavioral Pattern: Observer Pattern
@Component
public class IncidentLogObserver implements TrainStatusObserver {

    private final LogRepository logRepository;

    public IncidentLogObserver(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void onStatusChange(Train train, String oldStatus, String newStatus) {
        Log log = new Log();
        log.setAction("TRAIN_STATUS_CHANGE");
        log.setEntity("Train");
        log.setEntityId(train.getId());
        log.setDetails("Train " + train.getTrainNumber()
                + " status changed: " + oldStatus + " → " + newStatus
                + " [Observer Pattern — auto-logged]");
        // No user context — this is a system-triggered log
        logRepository.save(log);
    }
}
