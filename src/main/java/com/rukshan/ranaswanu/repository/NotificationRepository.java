package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}