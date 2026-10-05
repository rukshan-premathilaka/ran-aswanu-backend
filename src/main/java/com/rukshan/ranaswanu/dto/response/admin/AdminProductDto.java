package com.rukshan.ranaswanu.dto.response.admin;

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
public class AdminProductDto {
    private Long listId;
    private String productName;
    private String category;
    private String description;
    private String unitOfMeasurement;
    private BigDecimal pricePerUnit;
    private BigDecimal availableStock;
    private BigDecimal minimumOrderQuantity;
    private String deliveryOption;
    private String productImage;
    private Instant harvestedDate;
    private Boolean listingStatus;
    private boolean adminDisabled;
    private Instant adminDisabledAt;
    private String status;
    private Long farmerId;
    private String farmerName;
    private String farmerEmail;
    private boolean farmerActive;
    private Instant createdAt;
    private Instant updatedAt;
}
