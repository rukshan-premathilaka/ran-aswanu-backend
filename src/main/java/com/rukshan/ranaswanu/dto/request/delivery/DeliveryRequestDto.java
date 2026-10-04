package com.rukshan.ranaswanu.dto.request.delivery;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class DeliveryRequestDto {

    @NotBlank(message = "Pickup location is required")
    @Size(max = 255, message = "Pickup location must be at most 255 characters")
    private String pickupLocation;

    @NotBlank(message = "Destination is required")
    @Size(max = 255, message = "Destination must be at most 255 characters")
    private String destination;

    @NotNull(message = "Preferred date and time is required")
    @Future(message = "Date must be in the future")
    private Instant preferredDateTime;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must be at most 50 characters")
    private String vehicleType;

    @NotNull(message = "Estimated weight is required")
    @Positive(message = "Estimated weight must be above 0")
    private Long estimatedWeight;

    // optional: the frontend no longer sends it (stored as N/A)
    @Size(max = 50, message = "Size must be at most 50 characters")
    private String size;

    // optional: FARMER_REQUEST or VEHICLE_OFFER. If missing, it is chosen from the user's role.
    @Pattern(regexp = "FARMER_REQUEST|VEHICLE_OFFER",
             message = "Request type must be FARMER_REQUEST or VEHICLE_OFFER")
    private String requestType;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 500, message = "Special instructions must be at most 500 characters")
    private String specialInstructions;
}
