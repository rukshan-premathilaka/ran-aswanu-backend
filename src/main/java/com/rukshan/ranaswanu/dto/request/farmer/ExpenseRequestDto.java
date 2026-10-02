package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class ExpenseRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 50, message = "Title must be at most 50 characters")
    private String title;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be at most 50 characters")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be above 0")
    private Long amount;

    private Instant expenseDate;
}
