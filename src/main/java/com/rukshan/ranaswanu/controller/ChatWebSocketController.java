package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.chat.ChatMessageRequestDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatMessageResponseDto;
import com.rukshan.ranaswanu.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{chatId}")
    public void handleChatMessage(@DestinationVariable Long chatId,
                                   ChatMessageRequestDto request,
                                   Principal principal) {
        if (principal == null) {
            return;
        }
        try {
            ChatMessageResponseDto saved = chatService.saveMessage(principal.getName(), chatId, request.getContent());
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, saved);
        } catch (RuntimeException ex) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", safeError(ex))
            );
        }
    }

    public void pushNotification(Long userId, Object notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    private String safeError(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank()
                ? "We could not send your message. Please try again."
                : message;
    }
}
