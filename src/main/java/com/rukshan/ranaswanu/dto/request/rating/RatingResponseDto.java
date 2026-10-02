package com.rukshan.ranaswanu.dto.request.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
    private Long ratingId;
    private Long orderId;
    private Long revieweeId;   // the farmer who was rated
    private Integer score;
    private String comment;
    private Instant submittedAt;
}
