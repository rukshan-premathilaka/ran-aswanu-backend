package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CropRequestDto {

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Harvest quantity is required")
    private BigDecimal harvestQuantity;

    @NotNull(message = "Harvest date is required")
    private Instant harvestDate;

    private String notes;
}
