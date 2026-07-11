package com.rukshan.ranaswanu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}