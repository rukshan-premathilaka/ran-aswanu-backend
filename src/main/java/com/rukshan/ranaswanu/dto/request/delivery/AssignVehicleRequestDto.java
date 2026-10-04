package com.rukshan.ranaswanu.dto.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignVehicleRequestDto {
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;
}
