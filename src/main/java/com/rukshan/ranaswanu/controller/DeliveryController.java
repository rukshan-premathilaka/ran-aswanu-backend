package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeliveryController {

    // ---------------- CREATE DELIVERY REQUEST ----------------

    @PostMapping("/delivery-requests")
    public ResponseEntity<Map<String, Object>> createDeliveryRequest(@RequestBody Map<String, Object> requestData) {
        Long userId = requestData.get("userId") != null
                ? Long.parseLong(requestData.get("userId").toString())
                : 201L;

        String pickupLocation = (String) requestData.getOrDefault("pickupLocation", "Badulla");
        String destination = (String) requestData.getOrDefault("destination", "Colombo");
        String preferredDate = (String) requestData.getOrDefault("preferredDate", "2026-07-10");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestId", 701L);
        response.put("userId", userId);
        response.put("pickupLocation", pickupLocation);
        response.put("destination", destination);
        response.put("preferredDate", preferredDate);
        response.put("status", "OPEN");
        response.put("createdAt", "2026-07-07T10:00:00");
        response.put("message", "Delivery request created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------- GET USER'S DELIVERY REQUESTS ----------------

    @GetMapping("/delivery-requests/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryRequestsByUser(@PathVariable Long userId) {
        List<Map<String, Object>> requests = List.of(
                deliveryRequestSummary(701L, userId, "Badulla", "Colombo", "2026-07-10", "OPEN"),
                deliveryRequestSummary(702L, userId, "Bandarawela", "Kandy", "2026-07-05", "MATCHED"),
                deliveryRequestSummary(703L, userId, "Monaragala", "Colombo", "2026-06-28", "COMPLETED")
        );

        return ResponseEntity.ok(requests);
    }

    // ---------------- FIND MATCHING SHARED-DELIVERY REQUESTS ----------------

    @GetMapping("/delivery-requests/{requestId}/matches")
    public ResponseEntity<Map<String, Object>> getMatchingRequests(@PathVariable Long requestId) {
        List<Map<String, Object>> matches = List.of(
                matchData(702L, 205L, "Bandarawela", "Colombo", "2026-07-10", 0.85),
                matchData(703L, 206L, "Badulla", "Kandy", "2026-07-11", 0.62),
                matchData(704L, 207L, "Haputale", "Colombo", "2026-07-10", 0.78)
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestId", requestId);
        response.put("totalMatches", matches.size());
        response.put("matches", matches);

        return ResponseEntity.ok(response);
    }

    // ---------------- JOIN A SHARED DELIVERY ----------------

    @PostMapping("/delivery-requests/{requestId}/join")
    public ResponseEntity<Map<String, Object>> joinSharedDelivery(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> requestData) {

        Long userId = requestData.get("userId") != null
                ? Long.parseLong(requestData.get("userId").toString())
                : 205L;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestId", requestId);
        response.put("joinedUserId", userId);
        response.put("deliveryId", 801L);
        response.put("status", "MATCHED");
        response.put("sharedWith", List.of(201L, userId));
        response.put("estimatedCostSaving", "30%");
        response.put("message", "Successfully joined shared delivery");

        return ResponseEntity.ok(response);
    }

    // ---------------- TRACK DELIVERY STATUS ----------------

    @GetMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<Map<String, Object>> getDeliveryStatus(@PathVariable Long deliveryId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deliveryId", deliveryId);
        response.put("status", "IN_TRANSIT");
        response.put("currentLocation", "Nuwara Eliya");
        response.put("estimatedArrival", "2026-07-08T14:00:00");
        response.put("driverName", "K. Silva");
        response.put("vehicleNumber", "WP-CAB-4521");
        response.put("lastUpdated", "2026-07-07T09:30:00");

        return ResponseEntity.ok(response);
    }

    // ---------------- UPDATE DELIVERY STATUS ----------------

    @PatchMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<Map<String, Object>> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @RequestBody Map<String, String> requestData) {

        String newStatus = requestData.getOrDefault("status", "IN_TRANSIT");
        List<String> validStatuses = List.of("PENDING", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "CANCELLED");

        if (!validStatuses.contains(newStatus.toUpperCase())) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Invalid status value");
            error.put("allowedValues", validStatuses);
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deliveryId", deliveryId);
        response.put("status", newStatus.toUpperCase());
        response.put("updatedAt", "2026-07-07T16:00:00");
        response.put("message", "Delivery status updated to " + newStatus.toUpperCase());

        return ResponseEntity.ok(response);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> deliveryRequestSummary(Long requestId, Long userId, String pickup,
                                                       String destination, String date, String status) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("requestId", requestId);
        request.put("userId", userId);
        request.put("pickupLocation", pickup);
        request.put("destination", destination);
        request.put("preferredDate", date);
        request.put("status", status);
        return request;
    }

    private Map<String, Object> matchData(Long requestId, Long userId, String pickup,
                                          String destination, String date, double matchScore) {
        Map<String, Object> match = new LinkedHashMap<>();
        match.put("requestId", requestId);
        match.put("userId", userId);
        match.put("pickupLocation", pickup);
        match.put("destination", destination);
        match.put("preferredDate", date);
        match.put("matchScore", matchScore);
        return match;
    }
}