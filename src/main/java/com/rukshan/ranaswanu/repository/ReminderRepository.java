package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdAndReminderDate(Long userId, Instant reminderDate);


    List<Reminder> findByUserIdAndReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAsc(
            Long userId, Instant start, Instant end);
}
