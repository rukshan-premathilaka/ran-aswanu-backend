package com.rukshan.ranaswanu.service.impl;

import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Used by JwtAuthFilter to load the logged-in user from the token's email.
// All other auth logic lives in UserService.
@Service
public class AuthImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole() == null ? "ROLE_USER" : "ROLE_" + user.getRole())
                .accountExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}
