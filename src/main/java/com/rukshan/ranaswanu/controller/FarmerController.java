package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.farmer.*;
import com.rukshan.ranaswanu.dto.response.farmer.*;
import com.rukshan.ranaswanu.service.farmer.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    @Autowired private CropService cropService;
    @Autowired private FieldPlotService fieldPlotService;
    @Autowired private FarmActivityService farmActivityService;
    @Autowired private ExpenseService expenseService;
    @Autowired private LiveStockService liveStockService;

    // ---------------- CROPS ----------------

    @GetMapping("/crops")
    public ResponseEntity<List<CropResponseDto>> getMyCrops(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cropService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/crops")
    public ResponseEntity<CropResponseDto> addCrop(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CropRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.create(userDetails.getUsername(), requestData));
    }

    @GetMapping("/crops/{cropId}")
    public ResponseEntity<CropResponseDto> getCropById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cropId) {
        return ResponseEntity.ok(cropService.getById(userDetails.getUsername(), cropId));
    }

    @PutMapping("/crops/{cropId}")
    public ResponseEntity<CropResponseDto> updateCrop(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cropId,
            @RequestBody @Valid CropRequestDto requestData) {
        return ResponseEntity.ok(cropService.update(userDetails.getUsername(), cropId, requestData));
    }

    @DeleteMapping("/crops/{cropId}")
    public ResponseEntity<Void> deleteCrop(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cropId) {
        cropService.delete(userDetails.getUsername(), cropId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- FIELD PLOTS (stage/health tracking) ----------------

    @GetMapping("/field-plots")
    public ResponseEntity<List<FieldPlotResponseDto>> getMyFieldPlots(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(fieldPlotService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/field-plots")
    public ResponseEntity<FieldPlotResponseDto> addFieldPlot(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid FieldPlotRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldPlotService.create(userDetails.getUsername(), requestData));
    }

    @PutMapping("/field-plots/{fieldPlotId}")
    public ResponseEntity<FieldPlotResponseDto> updateFieldPlot(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long fieldPlotId,
            @RequestBody @Valid FieldPlotRequestDto requestData) {
        return ResponseEntity.ok(fieldPlotService.update(userDetails.getUsername(), fieldPlotId, requestData));
    }

    @DeleteMapping("/field-plots/{fieldPlotId}")
    public ResponseEntity<Void> deleteFieldPlot(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long fieldPlotId) {
        fieldPlotService.delete(userDetails.getUsername(), fieldPlotId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- ACTIVITIES ----------------

    @GetMapping("/activities")
    public ResponseEntity<List<FarmActivityResponseDto>> getMyActivities(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(farmActivityService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/activities")
    public ResponseEntity<FarmActivityResponseDto> addActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid FarmActivityRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmActivityService.create(userDetails.getUsername(), requestData));
    }

    // ---------------- EXPENSES ----------------

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getMyExpenses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(expenseService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponseDto> addExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ExpenseRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(userDetails.getUsername(), requestData));
    }

    // ---------------- LIVESTOCK ----------------

    @GetMapping("/livestock")
    public ResponseEntity<List<LiveStockResponseDto>> getMyLiveStock(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(liveStockService.listMine(userDetails.getUsername()));
    }

    @PostMapping("/livestock")
    public ResponseEntity<LiveStockResponseDto> addLiveStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid LiveStockRequestDto requestData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(liveStockService.create(userDetails.getUsername(), requestData));
    }

    @DeleteMapping("/livestock/{liveStockId}")
    public ResponseEntity<Void> deleteLiveStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long liveStockId) {
        liveStockService.delete(userDetails.getUsername(), liveStockId);
        return ResponseEntity.noContent().build();
    }
}