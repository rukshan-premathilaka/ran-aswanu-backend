package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.rating.RatingRequestDto;
import com.rukshan.ranaswanu.dto.response.rating.RatingResponseDto;
import com.rukshan.ranaswanu.dto.response.rating.UserReviewsResponseDto;
import com.rukshan.ranaswanu.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    // The buyer rates the farmer of a COMPLETED order (one rating per order)
    @PostMapping("/orders/{orderId}/rating")
    public ResponseEntity<RatingResponseDto> submitRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody @Valid RatingRequestDto requestData) {

        RatingResponseDto response = ratingService.submit(userDetails.getUsername(), orderId, requestData);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Average, total and the list of reviews received by one user
    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<UserReviewsResponseDto> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.listForUser(userId));
    }
}
