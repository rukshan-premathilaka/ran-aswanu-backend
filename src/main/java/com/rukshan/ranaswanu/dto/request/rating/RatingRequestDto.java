package com.rukshan.ranaswanu.dto.request.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RatingRequestDto {

    @NotNull(message = "Score must be between 1 and 5")
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    private Integer score;

    @Size(max = 500, message = "Comment must be at most 500 characters")
    private String comment;
}
