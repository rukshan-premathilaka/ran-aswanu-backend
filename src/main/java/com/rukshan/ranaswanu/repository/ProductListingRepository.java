package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.ProductListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductListingRepository extends JpaRepository<ProductListing, Long> {
}