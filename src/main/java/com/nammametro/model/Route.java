package com.nammametro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the 'routes' table.
 * Extends BaseEntity (OCP) — inherits id, createdAt, updatedAt.
 *
 * A route connects a source station to a destination station,
 * optionally passing through intermediate stations.
 *
 * SRP: This class has one responsibility — representing route data.
 */
@Entity
@Table(name = "routes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "intermediateStations")
public class Route extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    /** Source station (start of this route) */
    @ManyToOne
    @JoinColumn(name = "start_station_id", nullable = false)
    private Station startStation;

    /** Destination station (end of this route) */
    @ManyToOne
    @JoinColumn(name = "end_station_id", nullable = false)
    private Station endStation;

    /** Ordered list of intermediate stations between source and destination */
    @ManyToMany
    @JoinTable(
        name = "route_intermediate_stations",
        joinColumns = @JoinColumn(name = "route_id"),
        inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    @OrderColumn(name = "station_order")
    private List<Station> intermediateStations = new ArrayList<>();

    /** Total distance in kilometres */
    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    /** Estimated travel duration in minutes */
    @Column(name = "duration_min")
    private Integer durationMin;
}
