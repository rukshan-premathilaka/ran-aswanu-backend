package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.*;
import com.rukshan.ranaswanu.dto.request.auth.AuthForgotPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthResetPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthLoginDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthRegistrationDto;
import com.rukshan.ranaswanu.dto.response.*;
import com.rukshan.ranaswanu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateProfileDto requestData) {

        UserProfileDto updated = userService.updateUserProfile(userDetails.getUsername(), requestData);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/role")
    public ResponseEntity<RoleUpdateResponseDto> updateUserRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateRoleDto requestData) {

        RoleUpdateResponseDto response = userService.updateUserRole(userDetails.getUsername(), requestData.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/roles/transport")
    public ResponseEntity<RoleUpdateResponseDto> becomeTransport(
            @AuthenticationPrincipal UserDetails userDetails) {

        RoleUpdateResponseDto response = userService.becomeTransport(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponseDto> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordDto requestData) {

        userService.changePassword(userDetails.getUsername(), requestData);
        return ResponseEntity.ok(MessageResponseDto.builder().message("Password changed successfully").build());
    }

    @PostMapping("/me/picture")
    public ResponseEntity<ProfilePictureResponseDto> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        ProfilePictureResponseDto response = userService.updateProfilePicture(userDetails.getUsername(), file);
        return ResponseEntity.ok(response);
    }
}
