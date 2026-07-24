package com.rukshan.ranaswanu.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthResetPasswordDto {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}