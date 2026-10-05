package com.rukshan.ranaswanu.dto.request.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequestDto {

    @NotNull(message = "Product is required")
    private Long listId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be above 0")
    private BigDecimal quantity;
}
