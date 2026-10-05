package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.admin.AdminUserStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminUserDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // GET /api/admin/users?q=&role=&active=&from=2026-01-01&to=2026-12-31&page=0&size=20
    @GetMapping
    public ResponseEntity<PageResponseDto<AdminUserDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.list(q, role, active, from, to, page, size));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDto> get(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.get(userId));
    }

    // Body: { "active": false }  disables the account, { "active": true } enables it
    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserDto> setStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody @Valid AdminUserStatusRequestDto requestData) {
        return ResponseEntity.ok(adminUserService.setActive(userDetails.getUsername(), userId, requestData.getActive()));
    }
}
