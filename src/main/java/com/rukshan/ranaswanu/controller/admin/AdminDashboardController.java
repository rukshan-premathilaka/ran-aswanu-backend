package com.rukshan.ranaswanu.controller.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminDashboardSummaryDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminMonthlySummaryItemDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminYearlySummaryItemDto;
import com.rukshan.ranaswanu.service.admin.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ADMIN only (protected by /api/admin/** in AppConfig). Read-only.
@RestController
@RequestMapping("/api/admin/dashboard/summary")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardSummaryDto> summary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }

    // Optional ?year=2026 (default = current year)
    @GetMapping("/monthly")
    public ResponseEntity<List<AdminMonthlySummaryItemDto>> monthly(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(adminDashboardService.getMonthly(year));
    }

    @GetMapping("/yearly")
    public ResponseEntity<List<AdminYearlySummaryItemDto>> yearly() {
        return ResponseEntity.ok(adminDashboardService.getYearly());
    }
}
