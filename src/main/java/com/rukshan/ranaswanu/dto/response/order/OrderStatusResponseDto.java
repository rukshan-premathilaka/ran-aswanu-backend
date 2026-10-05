package com.rukshan.ranaswanu.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponseDto {
    private Long orderId;
    private String orderStatus;
}
