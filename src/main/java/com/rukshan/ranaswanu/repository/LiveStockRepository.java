package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.LiveStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiveStockRepository extends JpaRepository<LiveStock, Long> {
    List<LiveStock> findByUserId(Long userId);
    Optional<LiveStock> findByIdAndUserId(Long id, Long userId);
}