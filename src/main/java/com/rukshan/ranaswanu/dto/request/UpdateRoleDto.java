package com.rukshan.ranaswanu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleDto {
    @NotBlank(message = "Role is required")
    private String role;
}