package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.order.CheckoutRequestDto;
import com.rukshan.ranaswanu.dto.request.order.OrderStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.order.*;
import com.rukshan.ranaswanu.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/buyer/orders")
    public ResponseEntity<CheckoutResponseDto> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CheckoutRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(userDetails.getUsername(), requestData));
    }

    @GetMapping("/buyer/orders")
    public ResponseEntity<List<BuyerOrderSummaryDto>> getBuyerOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.listForBuyer(userDetails.getUsername()));
    }

    @GetMapping("/farmer/orders")
    public ResponseEntity<List<FarmerOrderSummaryDto>> getFarmerOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.listForFarmer(userDetails.getUsername()));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getById(userDetails.getUsername(), orderId));
    }

    @PatchMapping("/farmer/orders/{orderId}/status")
    public ResponseEntity<OrderStatusResponseDto> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody @Valid OrderStatusRequestDto requestData) {
        return ResponseEntity.ok(orderService.updateStatus(userDetails.getUsername(), orderId, requestData.getStatus()));
    }
}
