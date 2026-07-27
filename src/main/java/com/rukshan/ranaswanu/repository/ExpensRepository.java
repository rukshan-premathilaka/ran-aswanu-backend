package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Expens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpensRepository extends JpaRepository<Expens, Long> {
    List<Expens> findByUserIdOrderByExpenseDateDesc(Long userId);
}