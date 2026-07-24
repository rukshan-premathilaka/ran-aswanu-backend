package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}