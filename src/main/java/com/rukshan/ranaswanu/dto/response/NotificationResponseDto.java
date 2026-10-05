package com.rukshan.ranaswanu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private String title;
    private String message;

    // Without @JsonProperty, Jackson would write this field as "read" instead of "isRead"
    @JsonProperty("isRead")
    private boolean read;

    private Instant createdAt;
}
