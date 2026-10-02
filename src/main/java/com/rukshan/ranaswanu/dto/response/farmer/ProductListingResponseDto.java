package com.rukshan.ranaswanu.dto.response.farmer;

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
public class ProductListingResponseDto {
    private Long listId;
    private Long farmerId;
    private String farmerName;
    private String productName;
    private String category;
    private String description;
    private String unitOfMeasurement;
    private BigDecimal pricePerUnit;
    private BigDecimal availableStock;
    private BigDecimal minimumOrderQuantity;
    private Instant harvestedDate;
    private String deliveryOption;
    private String productImage;
    private Boolean listingStatus; // true = published/live, false = draft/unpublished
    private Instant createdAt;
    private Instant updatedAt;
}