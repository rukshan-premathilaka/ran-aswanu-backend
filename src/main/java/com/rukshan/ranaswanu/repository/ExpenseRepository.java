package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    // dashboard: total spent between two moments (start inclusive, end exclusive)
    @Query("select coalesce(sum(e.amount), 0L) from Expense e " +
           "where e.user.id = :userId and e.expenseDate >= :start and e.expenseDate < :end")
    long sumAmountBetween(@Param("userId") Long userId, @Param("start") Instant start, @Param("end") Instant end);
}