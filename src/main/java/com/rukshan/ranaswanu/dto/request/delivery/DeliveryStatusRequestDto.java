package com.rukshan.ranaswanu.dto.request.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryStatusRequestDto {

    @NotBlank(message = "Status is required")
    private String status;
}
