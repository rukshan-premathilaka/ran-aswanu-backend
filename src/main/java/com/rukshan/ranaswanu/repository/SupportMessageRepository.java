package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
}
