package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeliveryStatusResponseDto {
    private Long deliveryId;
    private String status;
    private Instant estimatedArrival;
    private Instant lastUpdated;
}
