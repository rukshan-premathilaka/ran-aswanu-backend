package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeliveryRequestResponseDto {
    private Long requestId;
    private String status;
    private String pickupLocation;
    private String destination;
    private Instant preferredDateTime;
    private Instant createdAt;
}
