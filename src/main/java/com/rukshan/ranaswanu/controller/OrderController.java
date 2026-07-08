package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    // ---------------- BUYER ORDERS ----------------

    @GetMapping("/buyers/{buyerId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByBuyer(@PathVariable Long buyerId) {
        List<Map<String, Object>> orders = List.of(
                orderSummary(501L, buyerId, 201L, "COMPLETED", 850.00, "2026-06-20"),
                orderSummary(502L, buyerId, 202L, "SHIPPED", 430.00, "2026-07-01"),
                orderSummary(503L, buyerId, 203L, "PENDING", 220.00, "2026-07-06")
        );

        return ResponseEntity.ok(orders);
    }

    // ---------------- FARMER ORDERS ----------------

    @GetMapping("/farmers/{farmerId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByFarmer(@PathVariable Long farmerId) {
        List<Map<String, Object>> orders = List.of(
                orderSummary(501L, 301L, farmerId, "COMPLETED", 850.00, "2026-06-20"),
                orderSummary(504L, 302L, farmerId, "ACCEPTED", 610.00, "2026-07-03"),
                orderSummary(505L, 303L, farmerId, "PENDING", 340.00, "2026-07-07")
        );

        return ResponseEntity.ok(orders);
    }

    // ---------------- ORDER DETAIL ----------------

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long orderId) {
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("buyerId", 201L);
        order.put("farmerId", 301L);
        order.put("orderStatus", "SHIPPED");
        order.put("orderDate", "2026-07-01");

        List<Map<String, Object>> items = List.of(
                orderItem(1L, 101L, "Fresh Tomatoes", 120.00, 5),
                orderItem(2L, 103L, "Basmati Rice", 220.00, 2)
        );
        order.put("items", items);

        double totalAmount = items.stream()
                .mapToDouble(item -> (double) item.get("lineTotal"))
                .sum();
        order.put("totalAmount", totalAmount);

        Map<String, Object> deliveryInfo = new LinkedHashMap<>();
        deliveryInfo.put("recipientName", "Rukshan Perera");
        deliveryInfo.put("address", "No 45, Temple Road, Badulla");
        deliveryInfo.put("phoneNumber", "0771234567");
        deliveryInfo.put("expectedDeliveryDate", "2026-07-10");
        order.put("deliveryInfo", deliveryInfo);

        Map<String, Object> paymentInfo = new LinkedHashMap<>();
        paymentInfo.put("method", "CARD");
        paymentInfo.put("status", "PAID");
        paymentInfo.put("transactionId", "TXN-2026-0701-004");
        order.put("paymentInfo", paymentInfo);

        return ResponseEntity.ok(order);
    }

    // ---------------- UPDATE ORDER STATUS ----------------

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> requestData) {

        String newStatus = requestData.getOrDefault("status", "ACCEPTED");
        List<String> validStatuses = List.of("ACCEPTED", "REJECTED", "SHIPPED", "COMPLETED");

        if (!validStatuses.contains(newStatus.toUpperCase())) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Invalid status value");
            error.put("allowedValues", validStatuses);
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", orderId);
        response.put("status", newStatus.toUpperCase());
        response.put("updatedAt", "2026-07-07T14:30:00");
        response.put("message", "Order status updated to " + newStatus.toUpperCase());

        return ResponseEntity.ok(response);
    }

    // ---------------- ORDER TIMELINE ----------------

    @GetMapping("/orders/{orderId}/timeline")
    public ResponseEntity<Map<String, Object>> getOrderTimeline(@PathVariable Long orderId) {
        List<Map<String, Object>> timeline = List.of(
                timelineEntry("PENDING", "2026-06-28T09:00:00", "Order placed by buyer"),
                timelineEntry("ACCEPTED", "2026-06-28T11:15:00", "Order accepted by farmer"),
                timelineEntry("SHIPPED", "2026-06-29T08:00:00", "Order shipped via courier"),
                timelineEntry("COMPLETED", "2026-07-01T16:45:00", "Order delivered and completed")
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", orderId);
        response.put("currentStatus", "COMPLETED");
        response.put("timeline", timeline);

        return ResponseEntity.ok(response);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> orderSummary(Long orderId, Long buyerId, Long farmerId,
                                             String status, double totalAmount, String orderDate) {
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("buyerId", buyerId);
        order.put("farmerId", farmerId);
        order.put("orderStatus", status);
        order.put("totalAmount", totalAmount);
        order.put("orderDate", orderDate);
        return order;
    }

    private Map<String, Object> orderItem(Long itemId, Long productId, String productName,
                                          double price, int quantity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemId", itemId);
        item.put("productId", productId);
        item.put("productName", productName);
        item.put("price", price);
        item.put("quantity", quantity);
        item.put("lineTotal", price * quantity);
        return item;
    }

    private Map<String, Object> timelineEntry(String status, String timestamp, String note) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("status", status);
        entry.put("timestamp", timestamp);
        entry.put("note", note);
        return entry;
    }
}