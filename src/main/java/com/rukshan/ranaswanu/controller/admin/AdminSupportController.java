package com.rukshan.ranaswanu.controller.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminSupportMessageResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ADMIN only (protected by /api/admin/** in AppConfig)
@RestController
@RequestMapping("/api/admin/support/messages")
public class AdminSupportController {

    @Autowired
    private AdminSupportService adminSupportService;

    @GetMapping
    public ResponseEntity<List<AdminSupportMessageResponseDto>> list(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminSupportService.list(search));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<AdminSupportMessageResponseDto> getById(@PathVariable Long messageId) {
        return ResponseEntity.ok(adminSupportService.getById(messageId));
    }
}
