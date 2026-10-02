package com.rukshan.ranaswanu.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatListResponseDto {
    private Long chatId;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private Instant updatedAt;
}
