package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.CustomerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerReviewRepository extends JpaRepository<CustomerReview, Long> {

    boolean existsByOrderId(Long orderId);

    // Reviews of one user, newest first (reviewer and order loaded in the same query)
    @Query("select r from CustomerReview r left join fetch r.reviewer left join fetch r.order " +
           "where r.user.id = :userId order by r.reviewDate desc, r.id desc")
    List<CustomerReview> findForUser(@Param("userId") Long userId);
}
