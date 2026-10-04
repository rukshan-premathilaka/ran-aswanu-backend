package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeliveryVehicleResponseDto {
    private Long vehicleId;
    private String vehicleName;
    private String vehicleType;
    private String registrationNumber;
    private Long capacityKg;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
