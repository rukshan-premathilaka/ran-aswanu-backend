package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByUserId(Long userId);
    Optional<Crop> findByIdAndUserId(Long id, Long userId);
}