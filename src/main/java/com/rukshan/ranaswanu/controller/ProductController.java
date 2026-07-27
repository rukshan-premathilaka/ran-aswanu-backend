package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.farmer.ProductListingRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.ProductListingResponseDto;
import com.rukshan.ranaswanu.service.farmer.ProductListingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductListingService productListingService;

    // ---------------- PUBLIC BROWSE ----------------

    @GetMapping("/products")
    public ResponseEntity<List<ProductListingResponseDto>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productListingService.browsePublished(category, keyword));
    }

    @GetMapping("/products/{listId}")
    public ResponseEntity<ProductListingResponseDto> getProductById(@PathVariable Long listId) {
        return ResponseEntity.ok(productListingService.getById(listId));
    }

    // ---------------- FARMER: OWN LISTINGS ----------------

    @PostMapping("/farmer/products")
    public ResponseEntity<ProductListingResponseDto> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ProductListingRequestDto requestData) {

        ProductListingResponseDto created = productListingService.create(userDetails.getUsername(), requestData);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/farmer/products")
    public ResponseEntity<List<ProductListingResponseDto>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productListingService.getMyListings(userDetails.getUsername()));
    }

    @PutMapping("/farmer/products/{listId}")
    public ResponseEntity<ProductListingResponseDto> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listId,
            @RequestBody @Valid ProductListingRequestDto requestData) {

        return ResponseEntity.ok(productListingService.update(userDetails.getUsername(), listId, requestData));
    }

    @PatchMapping("/farmer/products/{listId}/status")
    public ResponseEntity<ProductListingResponseDto> updateProductStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listId,
            @RequestBody java.util.Map<String, Boolean> requestData) {

        boolean published = Boolean.TRUE.equals(requestData.get("published"));
        return ResponseEntity.ok(productListingService.setPublished(userDetails.getUsername(), listId, published));
    }

    @DeleteMapping("/farmer/products/{listId}")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listId) {

        productListingService.delete(userDetails.getUsername(), listId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/farmer/products/{listId}/image")
    public ResponseEntity<ProductListingResponseDto> uploadProductImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(productListingService.uploadImage(userDetails.getUsername(), listId, file));
    }
}