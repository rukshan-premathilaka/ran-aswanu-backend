package com.rukshan.ranaswanu.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerOrderSummaryDto {
    private Long orderId;
    private String buyerName;
    private String contactNumber;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private Instant orderDate;
    private int itemCount;
    private String firstItemName;
}
