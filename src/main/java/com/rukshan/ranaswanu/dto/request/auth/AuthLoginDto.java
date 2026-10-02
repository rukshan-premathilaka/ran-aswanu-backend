package com.rukshan.ranaswanu.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginDto {

    private String username;

    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
