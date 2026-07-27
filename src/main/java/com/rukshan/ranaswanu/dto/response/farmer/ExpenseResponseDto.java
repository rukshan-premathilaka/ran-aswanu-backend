package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDto {
    private Long expenseId;
    private String title;
    private String category;
    private Long amount;
    private Instant expenseDate;
}