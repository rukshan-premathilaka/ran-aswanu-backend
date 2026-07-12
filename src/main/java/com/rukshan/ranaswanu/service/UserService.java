package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.ForgotPasswordDto;
import com.rukshan.ranaswanu.dto.request.ResetPasswordDto;
import com.rukshan.ranaswanu.dto.request.UpdateProfileDto;
import com.rukshan.ranaswanu.dto.request.UserLoginDto;
import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import com.rukshan.ranaswanu.dto.response.ProfilePictureResponseDto;
import com.rukshan.ranaswanu.dto.response.RoleUpdateResponseDto;
import com.rukshan.ranaswanu.dto.response.UserProfileDto;
import com.rukshan.ranaswanu.entities.PasswordResetToken;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.PasswordResetTokenRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import com.rukshan.ranaswanu.security.JwtUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordBaseUrl;

    private static final List<String> VALID_ROLES = List.of("FARMER", "BUYER", "TRANSPORT");

    // ---------------- AUTH ----------------

    public void register(UserRegistrationDto requestData) {
        User user = User.builder()
                .name(requestData.getUsername())
                .email(requestData.getEmail())
                .password(passwordEncoder.encode(requestData.getPassword()))
                .active(true)
                .createdAt(new Date())
                .build();
        userRepository.save(user);
    }

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(!user.isActive())
                .build();
    }

    public String login(UserLoginDto requestData) {
        String identifier = (requestData.getEmail() != null && !requestData.getEmail().isBlank())
                ? requestData.getEmail()
                : requestData.getUsername();

        if (identifier == null || identifier.isBlank()) {
            throw new BadCredentialsException("Username or email is required");
        }

        User user = userRepository.findByEmailOrName(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(!user.isActive())
                .build();

        if (!passwordEncoder.matches(requestData.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtUtil.generateToken(userDetails);
    }

    public void forgotPassword(ForgotPasswordDto requestData) {
        Optional<User> userOpt = userRepository.findByEmail(requestData.getEmail());

        if (userOpt.isEmpty()) {
            return; // deliberately silent — avoids leaking which emails are registered
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(new Date(System.currentTimeMillis() + (30 * 60 * 1000)))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = resetPasswordBaseUrl + "?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    public void resetPassword(ResetPasswordDto requestData) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(requestData.getToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BadCredentialsException("This reset token has already been used");
        }

        if (resetToken.getExpiryDate().before(new Date())) {
            throw new BadCredentialsException("This reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(requestData.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // ---------------- PROFILE (self-service) ----------------

    public UserProfileDto getUserProfile(String email) {
        User user = findUserByEmail(email);
        return toProfileDto(user);
    }

    public UserProfileDto updateUserProfile(String email, UpdateProfileDto requestData) {
        User user = findUserByEmail(email);

        if (requestData.getUsername() != null && !requestData.getUsername().isBlank()) {
            user.setName(requestData.getUsername());
        }
        if (requestData.getEmail() != null && !requestData.getEmail().isBlank()) {
            user.setEmail(requestData.getEmail());
        }
        if (requestData.getPhoneNumber() != null) {
            user.setPhoneNumber(requestData.getPhoneNumber());
        }
        if (requestData.getAddress() != null) {
            user.setAddress(requestData.getAddress());
        }

        userRepository.save(user);
        return toProfileDto(user);
    }

    public RoleUpdateResponseDto updateUserRole(String email, String newRole) {
        String normalizedRole = newRole.toUpperCase();

        if (!VALID_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role. Allowed values: " + VALID_ROLES);
        }

        User user = findUserByEmail(email);
        user.setRole(normalizedRole);
        userRepository.save(user);

        return RoleUpdateResponseDto.builder()
                .userId(user.getId())
                .role(user.getRole())
                .message("Role updated successfully")
                .build();
    }

    public ProfilePictureResponseDto updateProfilePicture(String email, MultipartFile file) {
        User user = findUserByEmail(email);

        if (user.getProfilePicture() != null) {
            fileStorageService.deleteFile(user.getProfilePicture());
        }

        String relativePath = fileStorageService.storeFile(file, "profile-pics");
        user.setProfilePicture(relativePath);
        userRepository.save(user);

        return ProfilePictureResponseDto.builder()
                .userId(user.getId())
                .profilePictureUrl("/files/" + relativePath)
                .message("Profile picture updated successfully")
                .build();
    }

    // ---------------- HELPERS ----------------

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private UserProfileDto toProfileDto(User user) {
        return UserProfileDto.builder()
                .userId(user.getId())
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profilePictureUrl(user.getProfilePicture() != null ? "/files/" + user.getProfilePicture() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}