package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeliveryRequestResponseDto {
    private Long requestId;
    private Long orderId;
    private String status;
    private String pickupLocation;
    private String destination;
    private Instant preferredDateTime;
    private Instant createdAt;
    private String requestType;
    private String vehicleType;
    private Long estimatedWeight;
    private String description;
    private Long deliveryId; // null until the request is matched
}
