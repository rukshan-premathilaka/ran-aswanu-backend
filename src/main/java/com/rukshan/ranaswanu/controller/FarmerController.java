package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.UserRegistrationDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FarmerController {

    // ---------------- CROPS ----------------

    @GetMapping("/farmers/{farmerId}/crops")
    public ResponseEntity<List<Map<String, Object>>> getCropsByFarmer(@PathVariable Long farmerId) {
        List<Map<String, Object>> crops = new ArrayList<>();

        crops.add(cropData(1L, farmerId, "Tomato", "Vegetable", "Growing", "2026-05-10", "2026-08-15", 0.5));
        crops.add(cropData(2L, farmerId, "Carrot", "Vegetable", "Harvested", "2026-03-01", "2026-06-01", 0.3));
        crops.add(cropData(3L, farmerId, "Paddy", "Grain", "Planted", "2026-06-20", "2026-11-01", 1.2));

        return ResponseEntity.ok(crops);
    }

    @PostMapping("/farmers/{farmerId}/crops")
    public ResponseEntity<Map<String, Object>> addCrop(
            @PathVariable Long farmerId,
            @RequestBody Map<String, Object> cropRequest) {

        Map<String, Object> created = cropData(
                4L,
                farmerId,
                (String) cropRequest.getOrDefault("cropName", "New Crop"),
                (String) cropRequest.getOrDefault("category", "Vegetable"),
                "Planted",
                LocalDate.now().toString(),
                LocalDate.now().plusMonths(3).toString(),
                cropRequest.get("landExtent") != null ? Double.parseDouble(cropRequest.get("landExtent").toString()) : 0.5
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/crops/{cropId}")
    public ResponseEntity<Map<String, Object>> getCropById(@PathVariable Long cropId) {
        Map<String, Object> crop = cropData(cropId, 101L, "Tomato", "Vegetable", "Growing", "2026-05-10", "2026-08-15", 0.5);
        return ResponseEntity.ok(crop);
    }

    @PutMapping("/crops/{cropId}")
    public ResponseEntity<Map<String, Object>> updateCrop(
            @PathVariable Long cropId,
            @RequestBody Map<String, Object> cropRequest) {

        Map<String, Object> updated = cropData(
                cropId,
                101L,
                (String) cropRequest.getOrDefault("cropName", "Tomato"),
                (String) cropRequest.getOrDefault("category", "Vegetable"),
                (String) cropRequest.getOrDefault("status", "Growing"),
                (String) cropRequest.getOrDefault("plantedDate", "2026-05-10"),
                (String) cropRequest.getOrDefault("expectedHarvestDate", "2026-08-15"),
                cropRequest.get("landExtent") != null ? Double.parseDouble(cropRequest.get("landExtent").toString()) : 0.5
        );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/crops/{cropId}")
    public ResponseEntity<Map<String, String>> deleteCrop(@PathVariable Long cropId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Crop with id " + cropId + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ---------------- ACTIVITIES ----------------

    @GetMapping("/farmers/{farmerId}/activities")
    public ResponseEntity<List<Map<String, Object>>> getActivitiesByFarmer(@PathVariable Long farmerId) {
        List<Map<String, Object>> activities = new ArrayList<>();

        activities.add(activityData(1L, farmerId, "Irrigation", "2026-06-01", "Watered tomato field for 2 hours"));
        activities.add(activityData(2L, farmerId, "Fertilizing", "2026-06-05", "Applied organic fertilizer to carrot field"));
        activities.add(activityData(3L, farmerId, "Pest Control", "2026-06-10", "Sprayed neem oil on paddy field"));

        return ResponseEntity.ok(activities);
    }

    @PostMapping("/farmers/{farmerId}/activities")
    public ResponseEntity<Map<String, Object>> addActivity(
            @PathVariable Long farmerId,
            @RequestBody Map<String, Object> activityRequest) {

        Map<String, Object> created = activityData(
                4L,
                farmerId,
                (String) activityRequest.getOrDefault("activityType", "General"),
                (String) activityRequest.getOrDefault("date", LocalDate.now().toString()),
                (String) activityRequest.getOrDefault("notes", "")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------------- EXPENSES ----------------

    @GetMapping("/farmers/{farmerId}/expenses")
    public ResponseEntity<List<Map<String, Object>>> getExpensesByFarmer(@PathVariable Long farmerId) {
        List<Map<String, Object>> expenses = new ArrayList<>();

        expenses.add(expenseData(1L, farmerId, "Seeds", 2500.00, "2026-05-01"));
        expenses.add(expenseData(2L, farmerId, "Fertilizer", 4200.00, "2026-05-15"));
        expenses.add(expenseData(3L, farmerId, "Labor", 6000.00, "2026-06-01"));

        return ResponseEntity.ok(expenses);
    }

    @PostMapping("/farmers/{farmerId}/expenses")
    public ResponseEntity<Map<String, Object>> addExpense(
            @PathVariable Long farmerId,
            @RequestBody Map<String, Object> expenseRequest) {

        Map<String, Object> created = expenseData(
                4L,
                farmerId,
                (String) expenseRequest.getOrDefault("category", "Miscellaneous"),
                expenseRequest.get("amount") != null ? Double.parseDouble(expenseRequest.get("amount").toString()) : 0.0,
                (String) expenseRequest.getOrDefault("date", LocalDate.now().toString())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------------- REPORTS ----------------

    @GetMapping("/farmers/{farmerId}/reports/productivity")
    public ResponseEntity<Map<String, Object>> getProductivityReport(@PathVariable Long farmerId) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("farmerId", farmerId);
        report.put("reportPeriod", "2026-01-01 to 2026-06-30");
        report.put("totalLandUsed", 2.0);
        report.put("totalYieldKg", 1450.5);

        List<Map<String, Object>> cropYields = new ArrayList<>();
        cropYields.add(yieldEntry("Tomato", 600.0, "kg"));
        cropYields.add(yieldEntry("Carrot", 350.5, "kg"));
        cropYields.add(yieldEntry("Paddy", 500.0, "kg"));
        report.put("cropYields", cropYields);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/farmers/{farmerId}/reports/expenses")
    public ResponseEntity<Map<String, Object>> getExpenseReport(@PathVariable Long farmerId) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("farmerId", farmerId);
        report.put("reportPeriod", "2026-01-01 to 2026-06-30");
        report.put("totalExpenses", 12700.00);

        List<Map<String, Object>> breakdown = new ArrayList<>();
        breakdown.add(expenseCategoryEntry("Seeds", 2500.00));
        breakdown.add(expenseCategoryEntry("Fertilizer", 4200.00));
        breakdown.add(expenseCategoryEntry("Labor", 6000.00));
        report.put("breakdown", breakdown);

        return ResponseEntity.ok(report);
    }

    // ---------------- HELPER METHODS (mock data builders) ----------------

    private Map<String, Object> cropData(Long id, Long farmerId, String name, String category,
                                         String status, String plantedDate, String harvestDate, double landExtent) {
        Map<String, Object> crop = new LinkedHashMap<>();
        crop.put("cropId", id);
        crop.put("farmerId", farmerId);
        crop.put("cropName", name);
        crop.put("category", category);
        crop.put("status", status);
        crop.put("plantedDate", plantedDate);
        crop.put("expectedHarvestDate", harvestDate);
        crop.put("landExtentAcres", landExtent);
        return crop;
    }

    private Map<String, Object> activityData(Long id, Long farmerId, String type, String date, String notes) {
        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("activityId", id);
        activity.put("farmerId", farmerId);
        activity.put("activityType", type);
        activity.put("date", date);
        activity.put("notes", notes);
        return activity;
    }

    private Map<String, Object> expenseData(Long id, Long farmerId, String category, double amount, String date) {
        Map<String, Object> expense = new LinkedHashMap<>();
        expense.put("expenseId", id);
        expense.put("farmerId", farmerId);
        expense.put("category", category);
        expense.put("amount", amount);
        expense.put("date", date);
        return expense;
    }

    private Map<String, Object> yieldEntry(String cropName, double yieldKg, String unit) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("cropName", cropName);
        entry.put("yield", yieldKg);
        entry.put("unit", unit);
        return entry;
    }

    private Map<String, Object> expenseCategoryEntry(String category, double amount) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("category", category);
        entry.put("amount", amount);
        return entry;
    }
}