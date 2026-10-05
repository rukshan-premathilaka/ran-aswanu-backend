package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.response.NotificationReadResponseDto;
import com.rukshan.ranaswanu.dto.response.NotificationResponseDto;
import com.rukshan.ranaswanu.entities.Notification;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.NotificationRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }


    public void create(Long userId, String title, String message) {
        try {
            User recipient = userRepository.findById(userId).orElse(null);
            if (recipient == null) {
                log.warn("Notification skipped: user {} not found", userId);
                return;
            }

            Notification n = new Notification();
            n.setUser(recipient);
            n.setTitle(cut(title, 50));
            n.setMessage(cut(message, 255));
            n.setIsRead(false);
            n.setCreatedAt(Instant.now());
            notificationRepository.save(n);

            NotificationResponseDto dto = toResponseDto(n);
            pushAfterCommit(userId, dto);
        } catch (Exception e) {
            log.error("Could not create notification for user {}", userId, e);
        }
    }

    public List<NotificationResponseDto> listMine(String email) {
        User user = requireUser(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public NotificationReadResponseDto markRead(String email, Long notificationId) {
        User user = requireUser(email);
        Notification n = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setIsRead(true);
        notificationRepository.save(n);
        return new NotificationReadResponseDto(n.getId(), true);
    }

    private void pushAfterCommit(Long userId, NotificationResponseDto dto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push(userId, dto);
                }
            });
        } else {
            push(userId, dto);
        }
    }

    private void push(Long userId, NotificationResponseDto dto) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
        } catch (Exception e) {
            log.warn("Live push failed for user {}", userId, e);
        }
    }

    private String cut(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1) + "\u2026";
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponseDto toResponseDto(Notification n) {
        return NotificationResponseDto.builder()
                .notificationId(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(Boolean.TRUE.equals(n.getIsRead()))
                .createdAt(n.getCreatedAt())
                .build();
    }
}
