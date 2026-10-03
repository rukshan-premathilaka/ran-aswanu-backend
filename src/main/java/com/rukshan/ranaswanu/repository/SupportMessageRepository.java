package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Admin dashboard
    @Query("select m.createdAt from SupportMessage m")
    List<Instant> findAllCreatedAt();
}
