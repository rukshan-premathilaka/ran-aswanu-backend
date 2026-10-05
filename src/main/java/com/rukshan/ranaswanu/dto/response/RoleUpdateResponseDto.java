package com.rukshan.ranaswanu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateResponseDto {
    private Long userId;
    private String role;
    private List<String> roles;
    private String message;
}
