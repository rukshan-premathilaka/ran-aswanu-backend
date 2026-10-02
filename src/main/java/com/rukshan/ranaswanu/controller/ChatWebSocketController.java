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

    // Server pushes a saved notification to a specific user (called by NotificationService)
    // Subscribed by client at: /topic/notifications/{userId}
    // Payload: { notificationId, title, message, isRead, createdAt }
    public void pushNotification(Long userId, Object notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
