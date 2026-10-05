package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropResponseDto {
    private Long cropId;
    private String cropName;
    private String category;
    private String unit;
    private BigDecimal harvestQuantity;
    private Instant harvestDate;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}