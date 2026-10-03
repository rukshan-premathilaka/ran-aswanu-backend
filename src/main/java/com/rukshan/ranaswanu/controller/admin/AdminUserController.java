package com.rukshan.ranaswanu.controller.admin;

import com.rukshan.ranaswanu.dto.request.admin.AdminStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminUserResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ADMIN only (protected by /api/admin/** in AppConfig)
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponseDto>> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminUserService.list(role, active, search));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponseDto> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getById(userId));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponseDto> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody AdminStatusRequestDto requestData) {

        if (requestData.getActive() == null) {
            throw new IllegalArgumentException("active is required (true or false)");
        }
        return ResponseEntity.ok(adminUserService.setActive(
                userDetails.getUsername(), userId, requestData.getActive()));
    }
}
