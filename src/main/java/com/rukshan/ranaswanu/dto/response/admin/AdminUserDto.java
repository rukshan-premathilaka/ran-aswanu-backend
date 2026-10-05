package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Long userId;
    private String username;
    private String email;
    private String role; // legacy primary role
    private List<String> roles;
    private boolean active;
    private String phoneNumber;
    private Date createdAt;

    // only filled on "view one user"; null in the list
    private String address;
    private String profilePictureUrl;
    private Long productCount;
    private Long orderCount;
    private Long supportMessageCount;
}
