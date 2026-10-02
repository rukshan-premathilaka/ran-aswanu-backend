package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.SupportMessageRequestDto;
import com.rukshan.ranaswanu.dto.response.SupportMessageResponseDto;
import com.rukshan.ranaswanu.entities.SupportMessage;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SupportService {

    @Autowired private SupportMessageRepository supportMessageRepository;
    @Autowired private UserRepository userRepository;

    public SupportMessageResponseDto create(String email, SupportMessageRequestDto requestData) {
        User user = requireUser(email);

        SupportMessage entity = new SupportMessage();
        entity.setUser(user);
        entity.setSubject(requestData.getSubject().trim());
        entity.setMessage(requestData.getMessage().trim());
        entity.setCreatedAt(Instant.now());

        supportMessageRepository.save(entity);
        return toResponseDto(entity);
    }

    // only my own messages, newest first
    public List<SupportMessageResponseDto> listMine(String email) {
        User user = requireUser(email);
        return supportMessageRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private SupportMessageResponseDto toResponseDto(SupportMessage m) {
        return SupportMessageResponseDto.builder()
                .messageId(m.getId())
                .subject(m.getSubject())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
