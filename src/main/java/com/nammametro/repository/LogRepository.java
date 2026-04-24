package com.nammametro.repository;

import com.nammametro.model.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Log entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for audit logs.
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByUserId(Long userId);

    List<Log> findByEntity(String entity);

    List<Log> findByAction(String action);

    /** Paginated logs ordered by most recent first */
    Page<Log> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Paginated logs filtered by action type */
    Page<Log> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /** Get all distinct action types for filter dropdown */
    @Query("SELECT DISTINCT l.action FROM Log l ORDER BY l.action")
    List<String> findDistinctActions();
}
