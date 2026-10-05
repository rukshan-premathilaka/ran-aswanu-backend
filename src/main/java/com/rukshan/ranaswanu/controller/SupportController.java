package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.SupportMessageRequestDto;
import com.rukshan.ranaswanu.dto.response.SupportMessageResponseDto;
import com.rukshan.ranaswanu.service.SupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/messages")
public class SupportController {

    @Autowired
    private SupportService supportService;

    @PostMapping
    public ResponseEntity<SupportMessageResponseDto> send(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SupportMessageRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.create(userDetails.getUsername(), requestData));
    }

    @GetMapping
    public ResponseEntity<List<SupportMessageResponseDto>> listMine(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(supportService.listMine(userDetails.getUsername()));
    }
}
