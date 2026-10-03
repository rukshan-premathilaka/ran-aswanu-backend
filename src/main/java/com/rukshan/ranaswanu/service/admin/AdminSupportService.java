package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminSupportMessageDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.entities.SupportMessage;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import com.rukshan.ranaswanu.repository.spec.AdminSupportSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AdminSupportService {

    @Autowired private SupportMessageRepository supportMessageRepository;

    @Transactional(readOnly = true)
    public PageResponseDto<AdminSupportMessageDto> list(String q, Long userId, LocalDate from, LocalDate to,
                                                        int page, int size) {
        AdminQueryUtil.checkDateRange(from, to);

        Page<SupportMessage> result = supportMessageRepository.findAll(
                AdminSupportSpecs.filter(q, userId,
                        AdminQueryUtil.startOfDay(from), AdminQueryUtil.startOfNextDay(to)),
                AdminQueryUtil.pageable(page, size));

        return PageResponseDto.of(result.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public AdminSupportMessageDto get(Long id) {
        return supportMessageRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Support message not found"));
    }

    private AdminSupportMessageDto toDto(SupportMessage m) {
        User sender = m.getUser();
        return AdminSupportMessageDto.builder()
                .messageId(m.getId())
                .subject(m.getSubject())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .userId(sender.getId())
                .username(sender.getName())
                .email(sender.getEmail())
                .role(sender.getRole())
                .build();
    }
}
