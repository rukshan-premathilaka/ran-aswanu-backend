package com.rukshan.ranaswanu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private boolean active;
    private String phoneNumber;
    private String address;
    private String profilePictureUrl;
    private Date createdAt;
}