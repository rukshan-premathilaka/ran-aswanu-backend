package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.CustomerReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerReviewRepository extends JpaRepository<CustomerReview, Long> {
}