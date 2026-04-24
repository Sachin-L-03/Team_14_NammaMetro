package com.nammametro.model;

import com.nammametro.model.enums.IncidentStatus;
import com.nammametro.model.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the 'incidents' table.
 * Represents an operational incident reported against a train.
 *
 * SRP: This class has one responsibility — representing incident data.
 */
@Entity
@Table(name = "incidents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title / summary of the incident */
    @Column(nullable = false, length = 150)
    private String title;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Severity: LOW, MEDIUM, HIGH */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity = Severity.LOW;

    /** Incident status: OPEN, RESOLVED */
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_status", nullable = false, length = 20)
    private IncidentStatus incidentStatus = IncidentStatus.OPEN;

    /** Legacy boolean kept for backward compatibility  */
    @Column(nullable = false)
    private Boolean resolved = false;

    /** Note added when resolving the incident */
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
