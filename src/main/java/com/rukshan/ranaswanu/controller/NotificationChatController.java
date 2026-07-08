package com.rukshan.ranaswanu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationChatController {

    // ---------------- NOTIFICATIONS ----------------

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<Map<String, Object>> getUserNotifications(@PathVariable Long userId) {
        List<Map<String, Object>> notifications = List.of(
                notificationData(1L, userId, "ORDER_UPDATE", "Your order #501 has been shipped", false, "2026-07-06T10:00:00"),
                notificationData(2L, userId, "RATING_RECEIVED", "You received a new 5-star rating", false, "2026-07-05T16:30:00"),
                notificationData(3L, userId, "DELIVERY_MATCH", "A shared delivery match was found for your request", true, "2026-07-03T09:15:00")
        );

        long unreadCount = notifications.stream().filter(n -> !(boolean) n.get("isRead")).count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("unreadCount", unreadCount);
        response.put("notifications", notifications);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long notificationId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("notificationId", notificationId);
        response.put("isRead", true);
        response.put("readAt", "2026-07-07T12:00:00");
        response.put("message", "Notification marked as read");

        return ResponseEntity.ok(response);
    }

    // ---------------- CHATS ----------------

    @GetMapping("/users/{userId}/chats")
    public ResponseEntity<List<Map<String, Object>>> getUserChats(@PathVariable Long userId) {
        List<Map<String, Object>> chats = List.of(
                chatThreadData(1L, userId, 301L, "Nimal Farms", "Is the rice still available?", "2026-07-06T18:20:00", 2),
                chatThreadData(2L, userId, 302L, "GreenLeaf Produce", "Sure, delivery on Friday works", "2026-07-05T14:10:00", 0),
                chatThreadData(3L, userId, 303L, "K. Silva (Transport)", "Picked up your order, on the way", "2026-07-07T09:30:00", 1)
        );

        return ResponseEntity.ok(chats);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<Map<String, Object>> getChatMessages(@PathVariable Long chatId) {
        List<Map<String, Object>> messages = List.of(
                messageData(1L, chatId, 201L, "Hi, is the basmati rice still available?", "2026-07-06T18:00:00"),
                messageData(2L, chatId, 301L, "Yes, we have 100kg in stock", "2026-07-06T18:05:00"),
                messageData(3L, chatId, 201L, "Great, I'll order 20kg", "2026-07-06T18:20:00")
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("chatId", chatId);
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, Object> requestData) {

        Long senderId = requestData.get("senderId") != null
                ? Long.parseLong(requestData.get("senderId").toString())
                : 201L;
        String content = (String) requestData.getOrDefault("content", "");

        Map<String, Object> message = messageData(4L, chatId, senderId, content, "2026-07-07T16:45:00");
        message.put("message", "Message sent successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // ---------------- HELPER METHODS ----------------

    private Map<String, Object> notificationData(Long id, Long userId, String type, String content,
                                                 boolean isRead, String createdAt) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("notificationId", id);
        notification.put("userId", userId);
        notification.put("type", type);
        notification.put("content", content);
        notification.put("isRead", isRead);
        notification.put("createdAt", createdAt);
        return notification;
    }

    private Map<String, Object> chatThreadData(Long chatId, Long userId, Long otherUserId, String otherUserName,
                                               String lastMessage, String lastMessageAt, int unreadCount) {
        Map<String, Object> chat = new LinkedHashMap<>();
        chat.put("chatId", chatId);
        chat.put("userId", userId);
        chat.put("otherUserId", otherUserId);
        chat.put("otherUserName", otherUserName);
        chat.put("lastMessage", lastMessage);
        chat.put("lastMessageAt", lastMessageAt);
        chat.put("unreadCount", unreadCount);
        return chat;
    }

    private Map<String, Object> messageData(Long messageId, Long chatId, Long senderId, String content, String sentAt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("messageId", messageId);
        message.put("chatId", chatId);
        message.put("senderId", senderId);
        message.put("content", content);
        message.put("sentAt", sentAt);
        return message;
    }
}