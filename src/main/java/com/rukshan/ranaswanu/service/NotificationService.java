package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.controller.ChatWebSocketController;
import com.rukshan.ranaswanu.dto.response.NotificationReadResponseDto;
import com.rukshan.ranaswanu.dto.response.NotificationResponseDto;
import com.rukshan.ranaswanu.entities.Notification;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.NotificationRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ChatWebSocketController chatWebSocketController;

    /**
     * Saves a notification for one user and pushes it live to /topic/notifications/{userId}.
     * Called by the order, rating and delivery services.
     * It never throws: a notification problem must not break an order or a rating.
     */
    public void create(Long userId, String title, String message) {
        try {
            // UserRepository is a CrudRepository, so use findById (getReferenceById is only in JpaRepository)
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
        // Someone else's notification looks the same as a missing one: 404
        Notification n = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setIsRead(true);
        notificationRepository.save(n);
        return new NotificationReadResponseDto(n.getId(), true);
    }

    // ---------------- HELPERS ----------------

    // If we are inside a bigger transaction (for example checkout), push only after it commits,
    // so the user is never told about something that was rolled back.
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
            chatWebSocketController.pushNotification(userId, dto);
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
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
