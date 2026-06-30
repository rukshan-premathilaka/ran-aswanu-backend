package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import com.rukshan.ranaswanu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDto requestData) {
        userService.register(requestData);
        return ResponseEntity.ok("Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserRegistrationDto requestData) {
        userService.login(requestData);
        return ResponseEntity.ok("Login Successfully");
    }
}