package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FieldPlotRequestDto {

    @NotNull
    private Long cropId;

    @NotBlank
    private String currentCrop;

    @NotBlank
    private String cropVariety;

    private BigDecimal areaUnit;

    @NotBlank
    private String growthStage; // Seedling | Vegetative | Flowering | Harvest

    @NotBlank
    private String healthCondition; // Excellent | Good | Alert

    private String fieldLogs;

    private LocalDate inspectionDate;
}