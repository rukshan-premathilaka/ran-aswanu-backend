package com.rukshan.ranaswanu.dto.response.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewsResponseDto {
    private Long userId;
    private double averageRating;   // 1 decimal, 0.0 when there are no reviews
    private int totalReviews;
    private List<ReviewItemDto> reviews;
}
