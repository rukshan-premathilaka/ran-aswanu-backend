package com.rukshan.ranaswanu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePictureResponseDto {
    private Long userId;
    private String profilePictureUrl;
    private String message;
}