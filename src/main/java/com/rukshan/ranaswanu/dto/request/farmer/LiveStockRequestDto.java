package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LiveStockRequestDto {
    @NotBlank
    private String category;

    private String breed;

    @NotNull
    private Long amount;
}