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