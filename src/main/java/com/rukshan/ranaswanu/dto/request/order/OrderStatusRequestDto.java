package com.rukshan.ranaswanu.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusRequestDto {

    @NotBlank(message = "Status is required")
    private String status;
}
