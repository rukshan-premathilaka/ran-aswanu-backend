package com.rukshan.ranaswanu.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private Long farmerId;
    private String farmerName;
    private String buyerName;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private Instant orderDate;
    private String deliveryAddress;
    private String contactNumber;
    private String paymentMethod;
    private String notes;
    private List<OrderItemResponseDto> items;
}
