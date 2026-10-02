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
public class BuyerOrderSummaryDto {
    private Long orderId;
    private Long farmerId;
    private String farmerName;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private Instant orderDate;
    private int itemCount;
    private String firstItemName;
}
