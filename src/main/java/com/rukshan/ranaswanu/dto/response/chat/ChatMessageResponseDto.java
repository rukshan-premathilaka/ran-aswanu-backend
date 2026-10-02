package com.rukshan.ranaswanu.dto.response.chat;

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
public class ChatMessageResponseDto {
    private Long messageId;
    private Long chatId;
    private Long senderId;
    private String content;
    private Instant sentAt;

    @JsonProperty("isRead")
    private boolean read;
}
