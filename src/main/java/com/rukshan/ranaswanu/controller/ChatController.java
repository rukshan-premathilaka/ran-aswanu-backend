package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.chat.ChatRequestDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatListResponseDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatMessageResponseDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatResponseDto;
import com.rukshan.ranaswanu.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> startChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChatRequestDto request) {
        return ResponseEntity.ok(chatService.startChat(userDetails.getUsername(), request.getOtherUserId()));
    }

    @GetMapping
    public ResponseEntity<List<ChatListResponseDto>> listChats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.listChats(userDetails.getUsername()));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> listMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.listMessages(userDetails.getUsername(), chatId));
    }
}
