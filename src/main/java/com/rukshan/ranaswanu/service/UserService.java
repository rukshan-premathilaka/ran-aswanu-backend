package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.ChangePasswordDto;
import com.rukshan.ranaswanu.dto.request.UpdateProfileDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthForgotPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthLoginDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthRegistrationDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthResetPasswordDto;
import com.rukshan.ranaswanu.dto.response.ProfilePictureResponseDto;
import com.rukshan.ranaswanu.dto.response.RoleUpdateResponseDto;
import com.rukshan.ranaswanu.dto.response.UserProfileDto;
import com.rukshan.ranaswanu.entities.PasswordResetToken;
import com.rukshan.ranaswanu.entities.Role;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ConflictException;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.PasswordResetTokenRepository;
import com.rukshan.ranaswanu.repository.RoleRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import com.rukshan.ranaswanu.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    // Creates a new account; email and username must be unique
    @Transactional
    public void register(AuthRegistrationDto requestData) {
        if (userRepository.existsByEmail(requestData.getEmail())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByName(requestData.getUsername())) {
            throw new ConflictException("Username already taken");
        }

        Role buyerRole = getRoleEntity("BUYER");
        User user = User.builder()
                .name(requestData.getUsername())
                .email(requestData.getEmail())
                .password(passwordEncoder.encode(requestData.getPassword()))
                .role("BUYER") // legacy primary-role field; capabilities are stored in user_roles
                .active(true)
                .createdAt(new Date())
                .build();
        user.addRole(buyerRole);
        userRepository.save(user);
    }

    // Checks email/username + password and returns a JWT; same error for both failures on purpose
    public String login(AuthLoginDto requestData) {
        String identifier = (requestData.getEmail() != null && !requestData.getEmail().isBlank())
                ? requestData.getEmail()
                : requestData.getUsername();

        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        User user = userRepository.findByEmailOrName(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(requestData.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(false)
                .disabled(false)
                .build();

        return jwtUtil.generateToken(userDetails);
    }

    // Sends a reset link; stays silent for unknown emails so we do not leak who is registered
    public void forgotPassword(AuthForgotPasswordDto requestData) {
        Optional<User> userOpt = userRepository.findByEmail(requestData.getEmail());

        if (userOpt.isEmpty()) {
            return;
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

    // Sets a new password from an emailed token; writes two tables so it is one transaction
    @Transactional
    public void resetPassword(AuthResetPasswordDto requestData) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(requestData.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new ResourceNotFoundException("This reset token has already been used");
        }

        if (resetToken.getExpiryDate().before(new Date())) {
            throw new ResourceNotFoundException("This reset token has expired");
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

    // Updates only the fields that were sent; username and email must stay unique
    public UserProfileDto updateUserProfile(String email, UpdateProfileDto requestData) {
        User user = findUserByEmail(email);

        String newName = requestData.getUsername();
        if (newName != null && !newName.isBlank() && !newName.equals(user.getName())) {
            if (userRepository.existsByName(newName)) {
                throw new ConflictException("Username already taken");
            }
            user.setName(newName);
        }

        String newEmail = requestData.getEmail();
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new ConflictException("Email already registered");
            }
            user.setEmail(newEmail);
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

    /**
     * Preferred self-service action: add TRANSPORT capability without removing BUYER.
     * Farmers are intentionally excluded from transport because the project business rule
     * requires a delivery partner to be non-farmer.
     */
    @Transactional
    public RoleUpdateResponseDto becomeTransport(String email) {
        User user = findUserByEmail(email);
        if (user.hasRole("ADMIN")) {
            throw new IllegalArgumentException("Administrator accounts cannot become delivery partners.");
        }
        if (user.hasRole("FARMER")) {
            throw new IllegalArgumentException("Farmers cannot become delivery partners.");
        }

        user.addRole(getRoleEntity("BUYER"));
        boolean alreadyTransport = user.hasRole("TRANSPORT");
        user.addRole(getRoleEntity("TRANSPORT"));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("BUYER");
        }
        userRepository.save(user);

        return roleResponse(user, alreadyTransport
                ? "You are already a delivery partner"
                : "You are now a delivery partner");
    }

    /**
     * Backward-compatible endpoint used by older frontend builds. It now manipulates
     * the role set while preserving the current business rules. New code should call
     * becomeTransport() for delivery onboarding.
     */
    @Transactional
    public RoleUpdateResponseDto updateUserRole(String email, String newRole) {
        String normalizedRole = newRole == null ? "" : newRole.trim().toUpperCase(Locale.ROOT);

        if (!VALID_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role. Allowed values: " + VALID_ROLES);
        }

        User user = findUserByEmail(email);
        if (user.hasRole("ADMIN")) {
            throw new IllegalArgumentException("Administrator accounts cannot change role here.");
        }

        if ("TRANSPORT".equals(normalizedRole)) {
            return becomeTransport(email);
        }

        if ("FARMER".equals(normalizedRole)) {
            if (user.hasRole("TRANSPORT")) {
                throw new IllegalArgumentException("Delivery partners cannot become farmers.");
            }
            user.addRole(getRoleEntity("BUYER"));
            user.addRole(getRoleEntity("FARMER"));
            user.setRole("FARMER");
        } else { // BUYER
            user.addRole(getRoleEntity("BUYER"));
            user.removeRole("FARMER");
            user.removeRole("TRANSPORT");
            user.setRole("BUYER");
        }

        userRepository.save(user);
        return roleResponse(user, "Roles updated");
    }

    // Changes the password after checking the old one (400, not 401, so the frontend does not log out)
    public void changePassword(String email, ChangePasswordDto requestData) {
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(requestData.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is wrong");
        }

        user.setPassword(passwordEncoder.encode(requestData.getNewPassword()));
        userRepository.save(user);
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
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .sorted()
                        .toList())
                .active(user.isActive())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profilePictureUrl(user.getProfilePicture() != null ? "/files/" + user.getProfilePicture() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private Role getRoleEntity(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role is not configured: " + roleName));
    }

    private RoleUpdateResponseDto roleResponse(User user, String message) {
        return RoleUpdateResponseDto.builder()
                .userId(user.getId())
                .role(user.getRole())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .sorted()
                        .toList())
                .message(message)
                .build();
    }
}
