package com.rukshan.ranaswanu.controller.admin;

import com.rukshan.ranaswanu.dto.request.admin.AdminStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminProductResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ADMIN only (protected by /api/admin/** in AppConfig)
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<List<AdminProductResponseDto>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean listingStatus,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminProductService.list(category, listingStatus, search));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<AdminProductResponseDto> getById(@PathVariable Long listId) {
        return ResponseEntity.ok(adminProductService.getById(listId));
    }

    @PatchMapping("/{listId}/status")
    public ResponseEntity<AdminProductResponseDto> updateStatus(
            @PathVariable Long listId,
            @RequestBody AdminStatusRequestDto requestData) {

        if (requestData.getActive() == null) {
            throw new IllegalArgumentException("active is required (true or false)");
        }
        return ResponseEntity.ok(adminProductService.setActive(listId, requestData.getActive()));
    }
}
