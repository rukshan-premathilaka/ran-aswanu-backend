package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FieldPlotRequestDto {

    // Optional: a plot can exist without a harvest record
    private Long cropId;

    @NotBlank(message = "Current crop is required")
    private String currentCrop;

    @NotBlank(message = "Crop variety is required")
    private String cropVariety;

    private BigDecimal areaUnit;

    @NotNull(message = "Growth stage is required")
    @Pattern(regexp = "Seedling|Vegetative|Flowering|Harvest",
            message = "Must be one of: Seedling, Vegetative, Flowering, Harvest")
    private String growthStage;

    @NotNull(message = "Health condition is required")
    @Pattern(regexp = "Excellent|Good|Alert",
            message = "Must be one of: Excellent, Good, Alert")
    private String healthCondition;

    private String fieldLogs;

    private LocalDate inspectionDate;
}
