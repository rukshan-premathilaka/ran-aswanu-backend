package com.rukshan.ranaswanu.service.impl;

import com.rukshan.ranaswanu.dto.request.auth.AuthForgotPasswordDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthLoginDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthRegistrationDto;
import com.rukshan.ranaswanu.dto.request.auth.AuthResetPasswordDto;
import com.rukshan.ranaswanu.entities.PasswordResetToken;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.PasswordResetTokenRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import com.rukshan.ranaswanu.security.JwtUtil;
import com.rukshan.ranaswanu.service.EmailService;
import com.rukshan.ranaswanu.service.FileStorageService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Primary
@Service
public class AuthImpl implements UserDetailsService {

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


    // ---------------- AUTH ----------------

    public void register(AuthRegistrationDto requestData) {
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
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public String login(AuthLoginDto requestData) {
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

    public void forgotPassword(AuthForgotPasswordDto requestData) {
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

    public void resetPassword(AuthResetPasswordDto requestData) {
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
}
