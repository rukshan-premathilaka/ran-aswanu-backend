package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ExpenseRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String category;

    @NotNull
    private Long amount;

    private Instant expenseDate;
}