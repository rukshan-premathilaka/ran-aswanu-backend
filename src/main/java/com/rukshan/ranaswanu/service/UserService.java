package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.UserLoginDto;
import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.UserRepository;
import com.rukshan.ranaswanu.security.JwtUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public void register(UserRegistrationDto requestData) {
        try {
            User user = User.builder()
                    .name(requestData.getUsername())
                    .email(requestData.getEmail())
                    .password(passwordEncoder.encode(requestData.getPassword()))
                    .active(true)
                    .createdAt(new Date())
                    .build();

            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Username or Email already exists");
        }
    }

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(!user.isActive())
                .build();
    }

    public String login(UserLoginDto requestData) {
        UserDetails userDetails = loadUserByUsername(requestData.getEmail());

        if (!passwordEncoder.matches(requestData.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtUtil.generateToken(userDetails);
    }
}