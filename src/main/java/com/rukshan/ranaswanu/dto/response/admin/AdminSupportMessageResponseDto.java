package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSupportMessageResponseDto {
    private Long messageId;
    private String subject;
    private String message;
    private Instant createdAt;
    private Long userId;
    private String username;
    private String email;
}
