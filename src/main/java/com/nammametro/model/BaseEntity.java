package com.nammametro.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ============================================================
 *  SOLID Principle: Open/Closed Principle (OCP)
 * ============================================================
 *
 *  This abstract base entity class is OPEN for extension (any new
 *  entity can extend it) but CLOSED for modification (adding a
 *  new entity type does not require changing this class).
 *
 *  All metro-domain entities (Station, Route, Train, etc.) extend
 *  this class to inherit common fields: id, createdAt, updatedAt.
 *
 *  To add a new entity type (e.g., Platform, Fare), simply create
 *  a new class that extends BaseEntity — no existing code changes.
 *
 *  SRP: This class has one responsibility — providing common
 *       identification and timestamp fields for all entities.
 * ============================================================
 */
@MappedSuperclass
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
