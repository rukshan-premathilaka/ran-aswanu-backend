package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.response.NotificationReadResponseDto;
import com.rukshan.ranaswanu.dto.response.NotificationResponseDto;
import com.rukshan.ranaswanu.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> listMine(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.listMine(userDetails.getUsername()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationReadResponseDto> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(userDetails.getUsername(), id));
    }
}
