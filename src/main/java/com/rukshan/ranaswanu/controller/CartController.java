package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CartController {

    // ---------------- GET CART ----------------

    @GetMapping("/buyers/{buyerId}/cart")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long buyerId) {
        List<Map<String, Object>> items = List.of(
                cartItemData(1L, 101L, "Fresh Tomatoes", 120.00, 5, "kg"),
                cartItemData(2L, 102L, "Organic Carrots", 90.00, 3, "kg"),
                cartItemData(3L, 103L, "Basmati Rice", 220.00, 2, "kg")
        );

        double subtotal = items.stream()
                .mapToDouble(item -> (double) item.get("lineTotal"))
                .sum();

        Map<String, Object> cart = new LinkedHashMap<>();
        cart.put("buyerId", buyerId);
        cart.put("items", items);
        cart.put("itemCount", items.size());
        cart.put("subtotal", subtotal);

        return ResponseEntity.ok(cart);
    }

    // ---------------- ADD ITEM ----------------

    @PostMapping("/buyers/{buyerId}/cart")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @PathVariable Long buyerId,
            @RequestBody Map<String, Object> requestData) {

        Long productId = requestData.get("productId") != null
                ? Long.parseLong(requestData.get("productId").toString())
                : 104L;

        String productName = (String) requestData.getOrDefault("productName", "New Product");
        double price = requestData.get("price") != null ? Double.parseDouble(requestData.get("price").toString()) : 0.0;
        int quantity = requestData.get("quantity") != null ? Integer.parseInt(requestData.get("quantity").toString()) : 1;
        String unit = (String) requestData.getOrDefault("unit", "kg");

        Map<String, Object> newItem = cartItemData(4L, productId, productName, price, quantity, unit);
        newItem.put("message", "Item added to cart");

        return ResponseEntity.status(HttpStatus.CREATED).body(newItem);
    }

    // ---------------- UPDATE QUANTITY ----------------

    @PutMapping("/buyers/{buyerId}/cart/{itemId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Long buyerId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> requestData) {

        int quantity = requestData.get("quantity") != null ? Integer.parseInt(requestData.get("quantity").toString()) : 1;

        Map<String, Object> updated = cartItemData(itemId, 101L, "Fresh Tomatoes", 120.00, quantity, "kg");
        updated.put("message", "Cart item updated successfully");

        return ResponseEntity.ok(updated);
    }

    // ---------------- REMOVE ITEM ----------------

    @DeleteMapping("/buyers/{buyerId}/cart/{itemId}")
    public ResponseEntity<Map<String, String>> removeCartItem(
            @PathVariable Long buyerId,
            @PathVariable Long itemId) {

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Item with id " + itemId + " removed from cart");

        return ResponseEntity.ok(response);
    }

    // ---------------- CHECKOUT ----------------

    @PostMapping("/buyers/{buyerId}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @PathVariable Long buyerId,
            @RequestBody Map<String, Object> requestData) {

        @SuppressWarnings("unchecked")
        Map<String, Object> deliveryInfo = requestData.get("deliveryInfo") != null
                ? (Map<String, Object>) requestData.get("deliveryInfo")
                : defaultDeliveryInfo();

        @SuppressWarnings("unchecked")
        Map<String, Object> paymentInfo = requestData.get("paymentInfo") != null
                ? (Map<String, Object>) requestData.get("paymentInfo")
                : defaultPaymentInfo();

        List<Map<String, Object>> orderedItems = List.of(
                cartItemData(1L, 101L, "Fresh Tomatoes", 120.00, 5, "kg"),
                cartItemData(2L, 102L, "Organic Carrots", 90.00, 3, "kg"),
                cartItemData(3L, 103L, "Basmati Rice", 220.00, 2, "kg")
        );

        double totalAmount = orderedItems.stream()
                .mapToDouble(item -> (double) item.get("lineTotal"))
                .sum();

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", 501L);
        order.put("buyerId", buyerId);
        order.put("items", orderedItems);
        order.put("totalAmount", totalAmount);
        order.put("deliveryInfo", deliveryInfo);
        order.put("paymentInfo", paymentInfo);
        order.put("orderStatus", "CONFIRMED");
        order.put("orderDate", "2026-07-07");
        order.put("message", "Order placed successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> cartItemData(Long itemId, Long productId, String productName,
                                             double price, int quantity, String unit) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemId", itemId);
        item.put("productId", productId);
        item.put("productName", productName);
        item.put("price", price);
        item.put("quantity", quantity);
        item.put("unit", unit);
        item.put("lineTotal", price * quantity);
        return item;
    }

    private Map<String, Object> defaultDeliveryInfo() {
        Map<String, Object> delivery = new LinkedHashMap<>();
        delivery.put("recipientName", "Rukshan Perera");
        delivery.put("address", "No 45, Temple Road, Badulla");
        delivery.put("phoneNumber", "0771234567");
        delivery.put("deliveryDate", "2026-07-10");
        return delivery;
    }

    private Map<String, Object> defaultPaymentInfo() {
        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("method", "CARD");
        payment.put("status", "PAID");
        payment.put("transactionId", "TXN-2026-0707-001");
        return payment;
    }
}