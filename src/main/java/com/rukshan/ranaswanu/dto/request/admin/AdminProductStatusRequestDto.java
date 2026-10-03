package com.rukshan.ranaswanu.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminProductStatusRequestDto {
    @NotNull(message = "disabled is required (true or false)")
    private Boolean disabled;
}
