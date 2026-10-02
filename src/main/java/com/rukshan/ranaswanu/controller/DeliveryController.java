package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.delivery.DeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryStatusRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.JoinDeliveryRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchesResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryRequestResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryStatusResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.JoinDeliveryResponseDto;
import com.rukshan.ranaswanu.service.DeliveryService;
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
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/delivery-requests")
    public ResponseEntity<DeliveryRequestResponseDto> createDeliveryRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid DeliveryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.createRequest(userDetails.getUsername(), request));
    }

    @GetMapping("/delivery-requests")
    public ResponseEntity<List<DeliveryRequestResponseDto>> getMyDeliveryRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(deliveryService.listMine(userDetails.getUsername()));
    }

    @GetMapping("/delivery-requests/{requestId}/matches")
    public ResponseEntity<DeliveryMatchesResponseDto> getMatchingRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(deliveryService.findMatches(userDetails.getUsername(), requestId));
    }

    @PostMapping("/delivery-requests/{requestId}/join")
    public ResponseEntity<JoinDeliveryResponseDto> joinSharedDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @RequestBody @Valid JoinDeliveryRequestDto request) {
        return ResponseEntity.ok(deliveryService.join(userDetails.getUsername(), requestId, request));
    }

    @GetMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<DeliveryStatusResponseDto> getDeliveryStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deliveryId) {
        return ResponseEntity.ok(deliveryService.getStatus(userDetails.getUsername(), deliveryId));
    }

    @PatchMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<DeliveryStatusResponseDto> updateDeliveryStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deliveryId,
            @RequestBody @Valid DeliveryStatusRequestDto request) {
        return ResponseEntity.ok(deliveryService.updateStatus(userDetails.getUsername(), deliveryId, request));
    }
}
