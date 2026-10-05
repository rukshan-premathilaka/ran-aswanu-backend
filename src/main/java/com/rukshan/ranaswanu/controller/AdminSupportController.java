package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.response.admin.AdminSupportMessageDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/support-messages")
public class AdminSupportController {

    @Autowired
    private AdminSupportService adminSupportService;

    // GET /api/admin/support-messages?q=&userId=&from=&to=&page=0&size=20
    @GetMapping
    public ResponseEntity<PageResponseDto<AdminSupportMessageDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminSupportService.list(q, userId, from, to, page, size));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<AdminSupportMessageDto> get(@PathVariable Long messageId) {
        return ResponseEntity.ok(adminSupportService.get(messageId));
    }
}
