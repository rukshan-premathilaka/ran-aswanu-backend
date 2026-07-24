package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.LiveStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveStockRepository extends JpaRepository<LiveStock, Long> {
}