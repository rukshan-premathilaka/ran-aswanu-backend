package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.LiveStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LiveStockRepository extends JpaRepository<LiveStock, Long> {
    List<LiveStock> findByUserId(Long userId);
    Optional<LiveStock> findByIdAndUserId(Long id, Long userId);

    @Query("select coalesce(sum(l.amount), 0L) from LiveStock l where l.user.id = :userId")
    long sumAmountByUserId(@Param("userId") Long userId);
}