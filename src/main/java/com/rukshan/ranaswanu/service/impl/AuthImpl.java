package com.rukshan.ranaswanu.service.impl;

import com.rukshan.ranaswanu.entities.Role;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuthImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<String> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(Role::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .map(name -> "ROLE_" + name)
                .toList();

        if (authorities.isEmpty() && user.getRole() != null && !user.getRole().isBlank()) {
            authorities = List.of("ROLE_" + user.getRole().trim().toUpperCase());
        }
        if (authorities.isEmpty()) {
            authorities = List.of("ROLE_USER");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .accountExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}
