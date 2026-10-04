package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// One card on the delivery board: another user's open request or vehicle offer
@Getter
@Builder
public class OpenDeliveryRequestDto {
    private Long requestId;
    private String requestType;
    private String description;
    private String vehicleType;
    private Long estimatedWeight;
    private Long userId;
    private String userName;
    private String pickupLocation;
    private String destination;
    private Instant preferredDateTime;
}
