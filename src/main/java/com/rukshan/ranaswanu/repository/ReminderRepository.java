package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    // One note per day: reminderDate is always the start of that day
    List<Reminder> findByUserIdAndReminderDate(Long userId, Instant reminderDate);

    // Notes of one month: start <= date < start of next month
    List<Reminder> findByUserIdAndReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAsc(
            Long userId, Instant start, Instant end);
}
