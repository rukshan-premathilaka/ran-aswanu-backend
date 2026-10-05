package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.response.admin.AdminMonthlyStatsDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminSummaryDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminYearlyStatsDto;
import com.rukshan.ranaswanu.service.admin.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryDto> summary() {
        return ResponseEntity.ok(adminStatsService.summary());
    }

    @GetMapping("/monthly")
    public ResponseEntity<AdminMonthlyStatsDto> monthly(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(adminStatsService.monthly(year));
    }

    @GetMapping("/yearly")
    public ResponseEntity<AdminYearlyStatsDto> yearly() {
        return ResponseEntity.ok(adminStatsService.yearly());
    }
}
