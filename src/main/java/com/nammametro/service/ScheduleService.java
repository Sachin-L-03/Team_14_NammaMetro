package com.nammametro.service;

import com.nammametro.model.Schedule;
import com.nammametro.model.enums.ScheduleStatus;
import com.nammametro.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Schedule operations.
 *
 * SRP: This class has one responsibility — encapsulating business rules for schedules.
 */
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> findById(Long id) {
        return scheduleRepository.findById(id);
    }

    public List<Schedule> findByTrainId(Long trainId) {
        return scheduleRepository.findByTrainId(trainId);
    }

    public List<Schedule> findByStationId(Long stationId) {
        return scheduleRepository.findByStationId(stationId);
    }

    public List<Schedule> findByDayOfWeek(String dayOfWeek) {
        return scheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    public List<Schedule> findByDate(LocalDate date) {
        return scheduleRepository.findByScheduleDate(date);
    }

    public List<Schedule> findTodayActive() {
        return scheduleRepository.findByScheduleDateAndScheduleStatus(
                LocalDate.now(), ScheduleStatus.ACTIVE);
    }

    public List<Schedule> findByStatus(ScheduleStatus status) {
        return scheduleRepository.findByScheduleStatus(status);
    }

    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public void deleteById(Long id) {
        scheduleRepository.deleteById(id);
    }

    public long count() {
        return scheduleRepository.count();
    }
}
