package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.*;
import com.rukshan.ranaswanu.dto.response.*;
import com.rukshan.ranaswanu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // ---------------- AUTH ----------------

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid UserRegistrationDto requestData) {
        userService.register(requestData);

        RegisterResponseDto response = RegisterResponseDto.builder()
                .message("Registered successfully")
                .username(requestData.getUsername())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid UserLoginDto requestData) {
        String token = userService.login(requestData);

        AuthResponseDto response = AuthResponseDto.builder()
                .token(token)
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(@RequestBody @Valid ForgotPasswordDto requestData) {
        userService.forgotPassword(requestData);

        MessageResponseDto response = MessageResponseDto.builder()
                .message("If an account with that email exists, a reset link has been sent.")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(@RequestBody @Valid ResetPasswordDto requestData) {
        userService.resetPassword(requestData);

        MessageResponseDto response = MessageResponseDto.builder()
                .message("Password reset successful")
                .build();

        return ResponseEntity.ok(response);
    }

    // ---------------- CURRENT USER (self-service, identified by token) ----------------

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

    @PostMapping("/me/picture")
    public ResponseEntity<ProfilePictureResponseDto> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        ProfilePictureResponseDto response = userService.updateProfilePicture(userDetails.getUsername(), file);
        return ResponseEntity.ok(response);
    }

    // ---------------- OTHER USERS (public-facing lookups) ----------------

    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<Map<String, Object>> getUserRatings(@PathVariable Long userId) {
        // TODO: mock data — replace once RatingController/RatingService is wired to real ratings table
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("averageRating", 4.6);
        response.put("totalRatings", 23);

        List<Map<String, Object>> ratings = List.of(
                ratingEntry(1L, 5, "Great quality vegetables, fast delivery"),
                ratingEntry(2L, 4, "Good produce, packaging could improve"),
                ratingEntry(3L, 5, "Very reliable farmer, will order again")
        );

        response.put("ratings", ratings);
        return ResponseEntity.ok(response);
    }

    // ---------------- HELPER ----------------

    private Map<String, Object> ratingEntry(Long id, int score, String comment) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("ratingId", id);
        entry.put("score", score);
        entry.put("comment", comment);
        entry.put("date", "2026-06-2" + id);
        return entry;
    }
}