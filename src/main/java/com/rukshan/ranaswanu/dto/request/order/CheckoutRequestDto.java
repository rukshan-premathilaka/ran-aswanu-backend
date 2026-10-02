package com.rukshan.ranaswanu.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequestDto {

    @NotEmpty(message = "Add at least one item")
    @Valid
    private List<OrderItemRequestDto> items;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 255, message = "Delivery address must be at most 255 characters")
    private String deliveryAddress;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number format")
    private String contactNumber;

    // Card numbers are never accepted or stored
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CASH_ON_DELIVERY|BANK_TRANSFER",
             message = "Payment method must be CASH_ON_DELIVERY or BANK_TRANSFER")
    private String paymentMethod;

    @Size(max = 255, message = "Notes must be at most 255 characters")
    private String notes;
}
