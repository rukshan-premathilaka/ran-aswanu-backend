package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RatingController {

    // ---------------- SUBMIT RATING ----------------

    @PostMapping("/orders/{orderId}/rating")
    public ResponseEntity<Map<String, Object>> submitRating(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> requestData) {

        int score = requestData.get("score") != null ? Integer.parseInt(requestData.get("score").toString()) : 5;
        String comment = (String) requestData.getOrDefault("comment", "");
        String revieweeType = (String) requestData.getOrDefault("revieweeType", "FARMER");
        Long revieweeId = requestData.get("revieweeId") != null
                ? Long.parseLong(requestData.get("revieweeId").toString())
                : 301L;

        if (score < 1 || score > 5) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Score must be between 1 and 5");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ratingId", 901L);
        response.put("orderId", orderId);
        response.put("revieweeId", revieweeId);
        response.put("revieweeType", revieweeType);
        response.put("score", score);
        response.put("comment", comment);
        response.put("submittedAt", "2026-07-07T15:20:00");
        response.put("message", "Rating submitted successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------- GET USER REVIEWS ----------------

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<Map<String, Object>> getUserReviews(@PathVariable Long userId) {
        List<Map<String, Object>> reviews = List.of(
                reviewData(1L, 501L, 201L, "BUYER", 5, "Excellent quality produce, very fresh!", "2026-06-21"),
                reviewData(2L, 502L, 202L, "BUYER", 4, "Good service, delivery was slightly delayed", "2026-07-02"),
                reviewData(3L, 504L, 302L, "BUYER", 5, "Reliable farmer, will order again", "2026-07-04")
        );

        double averageRating = reviews.stream()
                .mapToInt(r -> (int) r.get("score"))
                .average()
                .orElse(0.0);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        response.put("totalReviews", reviews.size());
        response.put("reviews", reviews);

        return ResponseEntity.ok(response);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> reviewData(Long ratingId, Long orderId, Long reviewerId,
                                           String reviewerType, int score, String comment, String date) {
        Map<String, Object> review = new LinkedHashMap<>();
        review.put("ratingId", ratingId);
        review.put("orderId", orderId);
        review.put("reviewerId", reviewerId);
        review.put("reviewerType", reviewerType);
        review.put("score", score);
        review.put("comment", comment);
        review.put("date", date);
        return review;
    }
}