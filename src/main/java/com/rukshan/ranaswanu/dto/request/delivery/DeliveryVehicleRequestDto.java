package com.rukshan.ranaswanu.dto.request.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryVehicleRequestDto {

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 100, message = "Vehicle name must be at most 100 characters")
    private String vehicleName;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must be at most 50 characters")
    private String vehicleType;

    @NotBlank(message = "Registration number is required")
    @Size(max = 50, message = "Registration number must be at most 50 characters")
    private String registrationNumber;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be above 0")
    private Long capacityKg;

    private Boolean active;
}
