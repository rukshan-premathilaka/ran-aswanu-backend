package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.dto.request.farmer.CalendarNoteRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.CalendarNoteResponseDto;
import com.rukshan.ranaswanu.service.farmer.CalendarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/farmer/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    // GET /api/farmer/calendar?year=2026&month=10  (only days that have a note)
    @GetMapping
    public ResponseEntity<List<CalendarNoteResponseDto>> getMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(calendarService.getMonth(userDetails.getUsername(), year, month));
    }

    @GetMapping("/{date}")
    public ResponseEntity<CalendarNoteResponseDto> getNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(calendarService.getNote(userDetails.getUsername(), date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<CalendarNoteResponseDto> saveNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody @Valid CalendarNoteRequestDto requestData) {
        return ResponseEntity.ok(calendarService.saveNote(userDetails.getUsername(), date, requestData));
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        calendarService.deleteNote(userDetails.getUsername(), date);
        return ResponseEntity.noContent().build();
    }
}
