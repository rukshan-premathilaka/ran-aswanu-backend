package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CropRequestDto {

    @NotBlank
    private String cropName;

    @NotBlank
    private String category;

    @NotBlank
    private String unit;

    @NotNull
    private BigDecimal harvestQuantity;

    @NotNull
    private Instant harvestDate;

    private String notes;
}