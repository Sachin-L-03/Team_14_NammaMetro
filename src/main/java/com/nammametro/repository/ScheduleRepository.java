package com.nammametro.repository;

import com.nammametro.model.Schedule;
import com.nammametro.model.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access layer for the Schedule entity.
 *
 * SRP: This interface has one responsibility — providing data access operations for schedules.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByTrainId(Long trainId);

    List<Schedule> findByStationId(Long stationId);

    List<Schedule> findByDayOfWeek(String dayOfWeek);

    List<Schedule> findByScheduleDate(LocalDate date);

    List<Schedule> findByScheduleDateAndScheduleStatus(LocalDate date, ScheduleStatus status);

    List<Schedule> findByScheduleStatus(ScheduleStatus status);
}
