package com.rukshan.ranaswanu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageResponseDto {
    private Long messageId;
    private String subject;
    private String message;
    private Instant createdAt;
}
