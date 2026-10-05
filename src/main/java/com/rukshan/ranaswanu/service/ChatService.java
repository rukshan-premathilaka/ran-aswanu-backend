package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.response.chat.ChatListResponseDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatMessageResponseDto;
import com.rukshan.ranaswanu.dto.response.chat.ChatResponseDto;
import com.rukshan.ranaswanu.entities.Chat;
import com.rukshan.ranaswanu.entities.Message;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.ChatRepository;
import com.rukshan.ranaswanu.repository.MessageRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatResponseDto startChat(String email, Long otherUserId) {
        User currentUser = requireUser(email);
        if (currentUser.getId().equals(otherUserId)) {
            throw new IllegalArgumentException("You cannot chat with yourself");
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        long firstId = Math.min(currentUser.getId(), otherUser.getId());
        long secondId = Math.max(currentUser.getId(), otherUser.getId());

        Chat chat = chatRepository.findByUserPair(firstId, secondId)
                .orElseGet(() -> createChat(currentUser, otherUser, firstId));
        return new ChatResponseDto(chat.getId(), otherUser.getId(), otherUser.getName());
    }

    @Transactional(readOnly = true)
    public List<ChatListResponseDto> listChats(String email) {
        User currentUser = requireUser(email);
        return chatRepository.findAllForUser(currentUser.getId()).stream()
                .map(chat -> toChatList(chat, currentUser.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> listMessages(String email, Long chatId) {
        User currentUser = requireUser(email);
        Chat chat = requireParticipant(chatId, currentUser.getId());
        return messageRepository.findAllByChatIdOrderBySentAtAsc(chat.getId()).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public void markChatRead(String email, Long chatId) {
        User currentUser = requireUser(email);
        requireParticipant(chatId, currentUser.getId());
        messageRepository.markReadForUser(chatId, currentUser.getId());
    }

    @Transactional
    public ChatMessageResponseDto saveMessage(String email, Long chatId, String content) {
        User sender = requireUser(email);
        Chat chat = requireParticipant(chatId, sender.getId());
        String cleanContent = content == null ? "" : content.trim();
        if (cleanContent.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (cleanContent.length() > 1000) {
            throw new IllegalArgumentException("Message must be at most 1000 characters");
        }

        Instant now = Instant.now();
        Message message = new Message();
        message.setChat(chat);
        message.setUser(sender);
        message.setContent(cleanContent);
        message.setIsRead(false);
        message.setSentAt(now);
        Message saved = messageRepository.save(message);

        chat.setUpdatedAt(now);
        chatRepository.save(chat);
        return toMessageResponse(saved);
    }

    private Chat createChat(User currentUser, User otherUser, long firstId) {
        Chat chat = new Chat();
        if (currentUser.getId().equals(firstId)) {
            chat.setUserOne(currentUser);
            chat.setUserTwo(otherUser);
        } else {
            chat.setUserOne(otherUser);
            chat.setUserTwo(currentUser);
        }
        Instant now = Instant.now();
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        return chatRepository.save(chat);
    }

    private ChatListResponseDto toChatList(Chat chat, Long currentUserId) {
        User other = chat.getUserOne().getId().equals(currentUserId) ? chat.getUserTwo() : chat.getUserOne();
        String lastMessage = messageRepository.findTopByChatIdOrderBySentAtDesc(chat.getId())
                .map(Message::getContent)
                .orElse(null);
        long unreadCount = messageRepository.countUnreadForUser(chat.getId(), currentUserId);
        return ChatListResponseDto.builder()
                .chatId(chat.getId())
                .otherUserId(other.getId())
                .otherUserName(other.getName())
                .lastMessage(lastMessage)
                .updatedAt(chat.getUpdatedAt())
                .unreadCount(unreadCount)
                .build();
    }

    private Chat requireParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        boolean participant = chat.getUserOne().getId().equals(userId) || chat.getUserTwo().getId().equals(userId);
        if (!participant) {
            throw new ResourceNotFoundException("Chat not found");
        }
        return chat;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ChatMessageResponseDto toMessageResponse(Message message) {
        return ChatMessageResponseDto.builder()
                .messageId(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getUser().getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .read(Boolean.TRUE.equals(message.getIsRead()))
                .build();
    }
}
