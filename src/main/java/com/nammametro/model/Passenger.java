package com.nammametro.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the 'passengers' table.
 * Extends user data with passenger-specific fields.
 *
 * SRP: This class has one responsibility — representing passenger data.
 */
@Entity
@Table(name = "passengers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 15)
    private String phone;

    @Column(length = 255)
    private String address;

    /** Whether the passenger has a Namma Metro Card (enables 10% discount) */
    @Column(name = "has_metro_card", nullable = false)
    private Boolean hasMetroCard = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
