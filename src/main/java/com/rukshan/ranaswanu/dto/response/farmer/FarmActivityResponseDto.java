package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmActivityResponseDto {
    private Long activityId;
    private String activity;
    private Boolean activityStatus;
    private Instant createdAt;
}