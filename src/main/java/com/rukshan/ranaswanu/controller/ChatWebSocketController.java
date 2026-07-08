package com.rukshan.ranaswanu.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Client sends to: /app/chat/{chatId}
    // Server broadcasts to: /topic/chat/{chatId}
    @MessageMapping("/chat/{chatId}")
    public void handleChatMessage(@DestinationVariable Long chatId, Map<String, Object> payload) {
        Map<String, Object> broadcastMessage = new LinkedHashMap<>();
        broadcastMessage.put("chatId", chatId);
        broadcastMessage.put("senderId", payload.get("senderId"));
        broadcastMessage.put("content", payload.get("content"));
        broadcastMessage.put("sentAt", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, Optional.of(broadcastMessage));
    }

    // Server pushes a notification to a specific user
    // Subscribed by client at: /topic/notifications/{userId}
    public void pushNotification(Long userId, String type, String content) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("userId", userId);
        notification.put("type", type);
        notification.put("content", content);
        notification.put("createdAt", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, Optional.of(notification));
    }
}