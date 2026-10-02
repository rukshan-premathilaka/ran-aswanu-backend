package com.rukshan.ranaswanu.dto.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinDeliveryRequestDto {

    @NotNull(message = "withRequestId is required")
    private Long withRequestId;
}
