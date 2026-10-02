package com.rukshan.ranaswanu.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDto {
    private List<OrderResponseDto> orders;
}
