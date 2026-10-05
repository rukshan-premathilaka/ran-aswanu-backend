package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminUserDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.OrderRepository;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import com.rukshan.ranaswanu.repository.spec.AdminUserSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class AdminUserService {

    private static final List<String> ROLES = List.of("FARMER", "BUYER", "TRANSPORT", "ADMIN");

    @Autowired private UserRepository userRepository;
    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SupportMessageRepository supportMessageRepository;

    @Transactional(readOnly = true)
    public PageResponseDto<AdminUserDto> list(String q, String role, Boolean active,
                                              LocalDate from, LocalDate to, int page, int size) {
        String normalizedRole = null;
        if (AdminQueryUtil.hasText(role)) {
            normalizedRole = role.trim().toUpperCase();
            if (!ROLES.contains(normalizedRole)) {
                throw new IllegalArgumentException("Invalid role filter. Allowed values: " + ROLES);
            }
        }
        AdminQueryUtil.checkDateRange(from, to);

        Instant fromInstant = AdminQueryUtil.startOfDay(from);
        Instant toExclusive = AdminQueryUtil.startOfNextDay(to);

        Page<User> result = userRepository.findAll(
                AdminUserSpecs.filter(q, normalizedRole, active, fromInstant, toExclusive),
                AdminQueryUtil.pageable(page, size));

        return PageResponseDto.of(result.map(u -> toDto(u, false)));
    }

    @Transactional(readOnly = true)
    public AdminUserDto get(Long id) {
        return toDto(requireUser(id), true);
    }

    // Disable (active = false) or enable (active = true). Repeating the same call is harmless.
    @Transactional
    public AdminUserDto setActive(String adminEmail, Long id, boolean active) {
        User target = requireUser(id);
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + adminEmail));

        if (!active) {
            if (target.getId().equals(admin.getId())) {
                throw new IllegalArgumentException("You cannot disable your own account.");
            }
            if (target.hasRole("ADMIN")) {
                throw new IllegalArgumentException("Administrator accounts cannot be disabled here.");
            }
        }

        if (target.isActive() != active) {
            target.setActive(active);
            userRepository.save(target);
        }
        return toDto(target, true);
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AdminUserDto toDto(User u, boolean withDetails) {
        AdminUserDto.AdminUserDtoBuilder b = AdminUserDto.builder()
                .userId(u.getId())
                .username(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .roles(u.getRoles().stream().map(com.rukshan.ranaswanu.entities.Role::getName).sorted().toList())
                .active(u.isActive())
                .phoneNumber(u.getPhoneNumber())
                .createdAt(u.getCreatedAt());

        if (withDetails) {
            b.address(u.getAddress())
                    .profilePictureUrl(u.getProfilePicture() != null ? "/files/" + u.getProfilePicture() : null)
                    .productCount(productListingRepository.countByUserId(u.getId()))
                    .orderCount(orderRepository.countByUserId(u.getId()))
                    .supportMessageCount(supportMessageRepository.countByUserId(u.getId()));
        }
        return b.build();
    }
}
