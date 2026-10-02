package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeliveryMatchResponseDto {
    private Long requestId;
    private String userName;
    private String pickupLocation;
    private String destination;
    private Instant preferredDateTime;
    private double matchScore;
    private int estimatedSavingPercent;
}
