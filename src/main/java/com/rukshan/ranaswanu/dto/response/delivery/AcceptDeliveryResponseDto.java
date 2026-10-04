package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcceptDeliveryResponseDto {
    private Long requestId;
    private Long deliveryId;
    private String status;
}
