package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.admin.AdminProductStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminProductDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.service.admin.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<PageResponseDto<AdminProductDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminProductService.list(q, category, status, farmerId, from, to, page, size));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<AdminProductDto> get(@PathVariable Long listId) {
        return ResponseEntity.ok(adminProductService.get(listId));
    }

    @PatchMapping("/{listId}/status")
    public ResponseEntity<AdminProductDto> setStatus(
            @PathVariable Long listId,
            @RequestBody @Valid AdminProductStatusRequestDto requestData) {
        return ResponseEntity.ok(adminProductService.setDisabled(listId, requestData.getDisabled()));
    }
}
