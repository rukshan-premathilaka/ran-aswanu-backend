package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LiveStockRequestDto {

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @Size(max = 100, message = "Breed must be at most 100 characters")
    private String breed;

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount must be 0 or more")
    private Long amount;
}
