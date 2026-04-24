package com.nammametro.model;

import com.nammametro.model.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA entity mapped to the 'schedules' table.
 * Represents a train's schedule: departure/arrival at station on a date.
 *
 * SRP: This class has one responsibility — representing schedule data.
 */
@Entity
@Table(name = "schedules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "day_of_week", length = 10)
    private String dayOfWeek;

    /** The specific date of this schedule */
    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    /** Schedule status — ACTIVE, CANCELLED, COMPLETED */
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", length = 20)
    private ScheduleStatus scheduleStatus = ScheduleStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
