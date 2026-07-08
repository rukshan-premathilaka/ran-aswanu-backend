package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.UserLoginDto;
import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import com.rukshan.ranaswanu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid UserRegistrationDto requestData) {
        userService.register(requestData);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Registered successfully");
        response.put("username", requestData.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid UserLoginDto requestData) {
        String token = userService.login(requestData);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Login successful"
        ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Logout successful");
        response.put("username", userDetails != null ? userDetails.getUsername() : "unknown");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> requestData) {
        String email = requestData.getOrDefault("email", "unknown@example.com");

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Password reset link sent to " + email);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> requestData) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Password reset successful");
        response.put("token", requestData.getOrDefault("token", "N/A"));

        return ResponseEntity.ok(response);
    }

    // ---------------- USERS ----------------

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestData) {

        String newRole = requestData.getOrDefault("role", "FARMER");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("role", newRole);
        response.put("message", "User role updated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long userId) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", userId);
        user.put("username", "rukshan_farmer");
        user.put("email", "rukshan@example.com");
        user.put("role", "FARMER");
        user.put("phoneNumber", "0771234567");
        user.put("address", "Badulla, Sri Lanka");
        user.put("createdAt", "2026-01-15");

        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestData) {

        Map<String, Object> updatedUser = new LinkedHashMap<>();
        updatedUser.put("userId", userId);
        updatedUser.put("username", requestData.getOrDefault("username", "rukshan_farmer"));
        updatedUser.put("email", requestData.getOrDefault("email", "rukshan@example.com"));
        updatedUser.put("phoneNumber", requestData.getOrDefault("phoneNumber", "0771234567"));
        updatedUser.put("address", requestData.getOrDefault("address", "Badulla, Sri Lanka"));
        updatedUser.put("message", "User updated successfully");

        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<Map<String, Object>> getUserRatings(@PathVariable Long userId) {
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

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", userDetails != null ? userDetails.getUsername() : "unknown");
        response.put("authorities", userDetails != null ? userDetails.getAuthorities() : List.of());
        response.put("message", "Currently logged in user");

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