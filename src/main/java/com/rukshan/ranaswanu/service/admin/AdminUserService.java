package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminUserResponseDto;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AdminUserService {

    private static final List<String> ROLES = List.of("FARMER", "BUYER", "TRANSPORT", "ADMIN");

    @Autowired
    private UserRepository userRepository;

    // All users, newest first. Every filter is optional.
    @Transactional(readOnly = true)
    public List<AdminUserResponseDto> list(String role, Boolean active, String search) {
        final String wantedRole = normalizeRole(role);
        final String keyword = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(u -> wantedRole == null || wantedRole.equalsIgnoreCase(u.getRole()))
                .filter(u -> active == null || u.isActive() == active)
                .filter(u -> keyword == null || contains(u.getName(), keyword) || contains(u.getEmail(), keyword))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponseDto getById(Long userId) {
        return toDto(requireUser(userId));
    }

    // Enable / disable only. Users are never deleted (many tables point to them).
    @Transactional
    public AdminUserResponseDto setActive(String adminEmail, Long userId, boolean active) {
        User user = requireUser(userId);

        if (!active && user.getEmail() != null && user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }

        user.setActive(active);
        userRepository.save(user);
        return toDto(user);
    }

    // ---------------- HELPERS ----------------

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if (!ROLES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid role. Allowed values: " + ROLES);
        }
        return normalized;
    }

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private AdminUserResponseDto toDto(User user) {
        return AdminUserResponseDto.builder()
                .userId(user.getId())
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profilePicture(user.getProfilePicture() != null ? "/files/" + user.getProfilePicture() : null)
                .createdAt(user.getCreatedAt() != null ? Instant.ofEpochMilli(user.getCreatedAt().getTime()) : null)
                .build();
    }
}
