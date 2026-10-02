package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FarmActivityRequestDto {

    @NotBlank(message = "Activity is required")
    @Size(max = 500, message = "Activity must be at most 500 characters")
    private String activity;
}
