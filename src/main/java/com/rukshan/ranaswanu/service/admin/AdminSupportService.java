package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminSupportMessageResponseDto;
import com.rukshan.ranaswanu.entities.SupportMessage;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Read-only: the Admin can view support messages (no reply / status / delete in version 1)
@Service
public class AdminSupportService {

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    // Newest first. Optional search in subject, message, username and email.
    @Transactional(readOnly = true)
    public List<AdminSupportMessageResponseDto> list(String search) {
        final String keyword = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        return supportMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt", "id")).stream()
                .filter(m -> keyword == null
                        || contains(m.getSubject(), keyword)
                        || contains(m.getMessage(), keyword)
                        || contains(m.getUser().getName(), keyword)
                        || contains(m.getUser().getEmail(), keyword))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminSupportMessageResponseDto getById(Long messageId) {
        SupportMessage message = supportMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Support message not found"));
        return toDto(message);
    }

    // ---------------- HELPERS ----------------

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private AdminSupportMessageResponseDto toDto(SupportMessage m) {
        return AdminSupportMessageResponseDto.builder()
                .messageId(m.getId())
                .subject(m.getSubject())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .userId(m.getUser().getId())
                .username(m.getUser().getName())
                .email(m.getUser().getEmail())
                .build();
    }
}
