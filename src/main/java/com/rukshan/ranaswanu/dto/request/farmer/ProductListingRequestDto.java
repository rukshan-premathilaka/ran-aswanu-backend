package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductListingRequestDto {

    @NotBlank
    private String productName;

    @NotBlank
    private String category;

    private String description;

    @NotBlank
    private String unitOfMeasurement; // matches ProductListing.unitOfMeasurement

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerUnit;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal availableStock;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal minimumOrderQuantity;

    private Instant harvestedDate;

    @NotBlank
    private String deliveryOption; // Pickup | Delivery | Both
}