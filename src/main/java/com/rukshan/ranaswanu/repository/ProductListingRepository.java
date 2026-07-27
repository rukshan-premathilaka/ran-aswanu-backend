package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.ProductListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductListingRepository extends JpaRepository<ProductListing, Long> {
    List<ProductListing> findByUserId(Long userId);
    Optional<ProductListing> findByIdAndUserId(Long id, Long userId);
    List<ProductListing> findByCategoryIgnoreCase(String category);
    List<ProductListing> findByProductNameContainingIgnoreCase(String keyword);
}