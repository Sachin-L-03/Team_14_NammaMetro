package com.nammametro.repository;

import com.nammametro.model.Ticket;
import com.nammametro.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the Ticket entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for tickets.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByPassengerId(Long passengerId);

    Page<Ticket> findByPassengerIdOrderByBookingTimeDesc(Long passengerId, Pageable pageable);

    List<Ticket> findTop5ByPassengerIdOrderByBookingTimeDesc(Long passengerId);

    List<Ticket> findByStatus(TicketStatus status);

    Optional<Ticket> findByQrCode(String qrCode);
}
