package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductListingRequestDto {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Category is required")
    private String category;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    @NotBlank(message = "Unit of measurement is required")
    @Size(max = 10, message = "Unit must be 10 characters or fewer")
    private String unitOfMeasurement; // example: kg

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be above 0")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Available stock is required")
    @DecimalMin(value = "0.0", message = "Available stock cannot be negative")
    private BigDecimal availableStock;

    @NotNull(message = "Minimum order quantity is required")
    @DecimalMin(value = "0.0", message = "Minimum order quantity cannot be negative")
    private BigDecimal minimumOrderQuantity;

    private Instant harvestedDate;

    @NotBlank(message = "Delivery option is required")
    @Pattern(regexp = "Pickup|Delivery|Both", message = "Delivery option must be Pickup, Delivery or Both")
    private String deliveryOption;
}
