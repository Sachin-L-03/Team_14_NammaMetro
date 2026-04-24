package com.nammametro.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity mapped to the 'stations' table.
 * Extends BaseEntity (OCP) — inherits id, createdAt, updatedAt.
 *
 * SRP: This class has one responsibility — representing station data.
 */
@Entity
@Table(name = "stations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Station extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 50)
    private String line; // e.g. "Purple Line", "Green Line"

    @Column(length = 255)
    private String location;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
