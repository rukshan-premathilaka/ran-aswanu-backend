package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FarmActivityRequestDto {
    @NotBlank
    private String activity;
}