package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldPlotResponseDto {
    private Long fieldPlotId;
    private Long cropId;
    private String cropName;
    private String currentCrop;
    private String cropVariety;
    private BigDecimal areaUnit;
    private String growthStage;
    private String healthCondition;
    private String fieldLogs;
    private LocalDate inspectionDate;
    private Instant createdAt;
    private Instant updatedAt;
}