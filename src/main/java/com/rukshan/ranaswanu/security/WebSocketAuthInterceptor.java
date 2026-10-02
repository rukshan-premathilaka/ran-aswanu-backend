package com.rukshan.ranaswanu.security;

import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    public WebSocketAuthInterceptor(JwtUtil jwtUtil,
                                    UserDetailsService userDetailsService,
                                    UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = first(accessor.getNativeHeader("Authorization"));
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Please log in again.");
        }

        String token = header.substring(7);
        String email;
        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please log in again.");
        }

        UserDetails details;
        try {
            details = userDetailsService.loadUserByUsername(email);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please log in again.");
        }
        if (!jwtUtil.isTokenValid(token, details)) {
            throw new IllegalArgumentException("Please log in again.");
        }

        accessor.setUser(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/notifications/")) {
            return;
        }
        if (accessor.getUser() == null) {
            throw new IllegalArgumentException("Please log in again.");
        }

        String suffix = destination.substring("/topic/notifications/".length());
        long requestedUserId;
        try {
            requestedUserId = Long.parseLong(suffix);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid notification destination");
        }

        User user = userRepository.findByEmail(accessor.getUser().getName())
                .orElseThrow(() -> new IllegalArgumentException("Please log in again."));
        if (!user.getId().equals(requestedUserId)) {
            throw new IllegalArgumentException("You cannot subscribe to another user's notifications");
        }
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }
}
