package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    // ---------------- BROWSE / SEARCH ----------------

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<Map<String, Object>> products = List.of(
                productData(1L, 101L, "Fresh Tomatoes", "Vegetable", 120.00, 50, "Badulla", "PUBLISHED"),
                productData(2L, 102L, "Organic Carrots", "Vegetable", 90.00, 30, "Bandarawela", "PUBLISHED"),
                productData(3L, 103L, "Basmati Rice", "Grain", 220.00, 100, "Monaragala", "PUBLISHED")
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("filters", filterEcho(category, location, keyword, minPrice, maxPrice));
        response.put("totalResults", products.size());
        response.put("products", products);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long productId) {
        Map<String, Object> product = productData(productId, 101L, "Fresh Tomatoes", "Vegetable", 120.00, 50, "Badulla", "PUBLISHED");
        product.put("description", "Freshly harvested, pesticide-free tomatoes grown in Badulla highlands.");
        product.put("images", List.of("tomatoes_1.jpg", "tomatoes_2.jpg"));
        product.put("createdAt", "2026-06-15");
        return ResponseEntity.ok(product);
    }

    // ---------------- CREATE / UPDATE / DELETE ----------------

    @PostMapping("/farmers/{farmerId}/products")
    public ResponseEntity<Map<String, Object>> createProduct(
            @PathVariable Long farmerId,
            @RequestBody Map<String, Object> requestData) {

        Map<String, Object> created = productData(
                4L,
                farmerId,
                (String) requestData.getOrDefault("productName", "New Product"),
                (String) requestData.getOrDefault("category", "Vegetable"),
                requestData.get("price") != null ? Double.parseDouble(requestData.get("price").toString()) : 0.0,
                requestData.get("quantity") != null ? Integer.parseInt(requestData.get("quantity").toString()) : 0,
                (String) requestData.getOrDefault("location", "Unknown"),
                "DRAFT"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> requestData) {

        Map<String, Object> updated = productData(
                productId,
                101L,
                (String) requestData.getOrDefault("productName", "Fresh Tomatoes"),
                (String) requestData.getOrDefault("category", "Vegetable"),
                requestData.get("price") != null ? Double.parseDouble(requestData.get("price").toString()) : 120.00,
                requestData.get("quantity") != null ? Integer.parseInt(requestData.get("quantity").toString()) : 50,
                (String) requestData.getOrDefault("location", "Badulla"),
                (String) requestData.getOrDefault("status", "PUBLISHED")
        );

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
            @PathVariable Long productId,
            @RequestBody Map<String, String> requestData) {

        String newStatus = requestData.getOrDefault("status", "PUBLISHED");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", productId);
        response.put("status", newStatus);
        response.put("message", "Product status updated to " + newStatus);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Product with id " + productId + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ---------------- FARMER'S OWN LISTINGS ----------------

    @GetMapping("/farmers/{farmerId}/products")
    public ResponseEntity<List<Map<String, Object>>> getProductsByFarmer(@PathVariable Long farmerId) {
        List<Map<String, Object>> products = List.of(
                productData(1L, farmerId, "Fresh Tomatoes", "Vegetable", 120.00, 50, "Badulla", "PUBLISHED"),
                productData(2L, farmerId, "Organic Carrots", "Vegetable", 90.00, 30, "Badulla", "DRAFT"),
                productData(3L, farmerId, "Basmati Rice", "Grain", 220.00, 100, "Badulla", "SOLD")
        );

        return ResponseEntity.ok(products);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> productData(Long productId, Long farmerId, String name, String category,
                                            double price, int quantity, String location, String status) {
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("productId", productId);
        product.put("farmerId", farmerId);
        product.put("productName", name);
        product.put("category", category);
        product.put("price", price);
        product.put("quantity", quantity);
        product.put("location", location);
        product.put("status", status);
        return product;
    }

    private Map<String, Object> filterEcho(String category, String location, String keyword,
                                           Double minPrice, Double maxPrice) {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("category", category);
        filters.put("location", location);
        filters.put("keyword", keyword);
        filters.put("minPrice", minPrice);
        filters.put("maxPrice", maxPrice);
        return filters;
    }
}