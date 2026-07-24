package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.auth.AuthForgotPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthResetPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthLoginDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthRegistrationDto;
import com.rukshan.ranaswanu.dto.response.*;
import com.rukshan.ranaswanu.dto.response.auth.AuthResponseDto;
import com.rukshan.ranaswanu.dto.response.auth.RegisterResponseDto;
import com.rukshan.ranaswanu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class Auth {

    @Autowired
    private UserService userService;

    // ---------------- AUTH ----------------

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid AuthRegistrationDto requestData) {
        userService.register(requestData);

        RegisterResponseDto response = RegisterResponseDto.builder()
                .message("Registered successfully")
                .username(requestData.getUsername())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthLoginDto requestData) {
        String token = userService.login(requestData);

        AuthResponseDto response = AuthResponseDto.builder()
                .token(token)
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(@RequestBody @Valid AuthForgotPasswordDto requestData) {
        userService.forgotPassword(requestData);

        MessageResponseDto response = MessageResponseDto.builder()
                .message("If an account with that email exists, a reset link has been sent.")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(@RequestBody @Valid AuthResetPasswordDto requestData) {
        userService.resetPassword(requestData);

        MessageResponseDto response = MessageResponseDto.builder()
                .message("Password reset successful")
                .build();

        return ResponseEntity.ok(response);
    }
}