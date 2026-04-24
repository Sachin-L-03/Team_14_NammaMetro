package com.nammametro.model;

import com.nammametro.model.enums.TrainStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity mapped to the 'trains' table.
 * Extends BaseEntity (OCP) — inherits id, createdAt, updatedAt.
 *
 * SRP: This class has one responsibility — representing train data.
 */
@Entity
@Table(name = "trains")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Train extends BaseEntity {

    @Column(name = "train_number", nullable = false, unique = true, length = 20)
    private String trainNumber;

    /** Display name for the train */
    @Column(length = 100)
    private String name;

    /** Assigned route */
    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainStatus status = TrainStatus.SCHEDULED;

    @Column(nullable = false)
    private Integer capacity = 0;
}
