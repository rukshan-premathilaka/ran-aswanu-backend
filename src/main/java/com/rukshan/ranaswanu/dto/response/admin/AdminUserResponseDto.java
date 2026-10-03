package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Safe user view for the Admin: no password, no hash, no reset token
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private boolean active;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private Instant createdAt;
}
