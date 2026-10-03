package com.rukshan.ranaswanu.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusRequestDto {
    @NotNull(message = "active is required (true or false)")
    private Boolean active;
}
