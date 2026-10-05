package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long>,
        JpaSpecificationExecutor<SupportMessage> {

    List<SupportMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ---- admin: counts ----
    long countByUserId(Long userId);

    @Query("select count(m) from SupportMessage m where m.createdAt >= :from and m.createdAt < :to")
    long countCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);
}
