package com.rukshan.ranaswanu.dto.request.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDto {
    private Long ratingId;
    private Long orderId;
    private String reviewerName;
    private Integer score;
    private String comment;
    private String date;   // yyyy-MM-dd
}
