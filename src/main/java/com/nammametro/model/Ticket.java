package com.nammametro.model;

import com.nammametro.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity mapped to the 'tickets' table.
 * Represents a booked metro ticket with QR code for validation.
 *
 * SRP: This class has one responsibility — representing ticket data.
 */
@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "source_station_id", nullable = false)
    private Station sourceStation;

    @ManyToOne
    @JoinColumn(name = "dest_station_id", nullable = false)
    private Station destStation;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.BOOKED;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    /** Unique QR code string (UUID-based) for ticket validation */
    @Column(name = "qr_code", unique = true, length = 100)
    private String qrCode;

    /** Amount refunded on cancellation */
    @Column(name = "refund_amount", precision = 8, scale = 2)
    private BigDecimal refundAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
