package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.FarmActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmActivityRepository extends JpaRepository<FarmActivity, Long> {
    List<FarmActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
}