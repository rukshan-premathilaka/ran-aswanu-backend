package com.rukshan.ranaswanu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadResponseDto {
    private Long notificationId;

    @JsonProperty("isRead")
    private boolean read;
}
