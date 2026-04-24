package com.nammametro.service;

import com.nammametro.model.Log;
import com.nammametro.model.User;
import com.nammametro.repository.LogRepository;
import com.nammametro.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Records audit log entries whenever an admin performs a create, update, or delete action.
 *
 * SRP: This class has one responsibility — creating audit trail entries.
 */
@Service
public class AuditLogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public AuditLogService(LogRepository logRepository, UserRepository userRepository) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    /**
     * Logs an admin action.
     *
     * @param action    the action performed (CREATE, UPDATE, DELETE)
     * @param entity    the entity type (Station, Route, Train)
     * @param entityId  the ID of the affected entity
     * @param details   optional description of the action
     */
    public void logAction(String action, String entity, Long entityId, String details) {
        Log log = new Log();
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDetails(details);

        // Resolve the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(log::setUser);
        }

        logRepository.save(log);
    }
}
