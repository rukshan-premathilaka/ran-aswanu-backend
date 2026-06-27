package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
}