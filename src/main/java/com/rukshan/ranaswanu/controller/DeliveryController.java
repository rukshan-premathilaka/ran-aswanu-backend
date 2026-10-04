package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.delivery.AssignVehicleRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryStatusRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryVehicleRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.JoinDeliveryRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.AcceptDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchesResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryRequestResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryStatusResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryVehicleResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.JoinDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.OpenDeliveryRequestDto;
import com.rukshan.ranaswanu.service.DeliveryService;
import com.rukshan.ranaswanu.service.DeliveryVehicleService;
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

    @Autowired private DeliveryService deliveryService;
    @Autowired private DeliveryVehicleService deliveryVehicleService;

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

    // Delivery board: type CUSTOMER_REQUEST is used by transport users for both open and already-shared requests.
    @GetMapping("/delivery-requests/open")
    public ResponseEntity<List<OpenDeliveryRequestDto>> getOpenDeliveryRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(deliveryService.listOpen(userDetails.getUsername(), type));
    }

    // Existing clients may omit the body; the new flow sends {"vehicleId": ...}.
    @PostMapping("/delivery-requests/{requestId}/accept")
    public ResponseEntity<AcceptDeliveryResponseDto> acceptDeliveryRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @RequestBody(required = false) AssignVehicleRequestDto assignment) {
        return ResponseEntity.ok(deliveryService.accept(userDetails.getUsername(), requestId, assignment));
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

    // ---------------- TRANSPORT VEHICLE CRUD ----------------

    @GetMapping("/delivery/vehicles")
    public ResponseEntity<List<DeliveryVehicleResponseDto>> listVehicles(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(deliveryVehicleService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/delivery/vehicles")
    public ResponseEntity<DeliveryVehicleResponseDto> createVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid DeliveryVehicleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryVehicleService.create(userDetails.getUsername(), request));
    }

    @PutMapping("/delivery/vehicles/{vehicleId}")
    public ResponseEntity<DeliveryVehicleResponseDto> updateVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vehicleId,
            @RequestBody @Valid DeliveryVehicleRequestDto request) {
        return ResponseEntity.ok(deliveryVehicleService.update(userDetails.getUsername(), vehicleId, request));
    }

    @DeleteMapping("/delivery/vehicles/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vehicleId) {
        deliveryVehicleService.delete(userDetails.getUsername(), vehicleId);
        return ResponseEntity.noContent().build();
    }
}
