package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.FieldPlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FieldPlotRepository extends JpaRepository<FieldPlot, Long> {
    List<FieldPlot> findByUserId(Long userId);
    Optional<FieldPlot> findByIdAndUserId(Long id, Long userId);
    Optional<FieldPlot> findByCropIdAndUserId(Long cropId, Long userId);
}