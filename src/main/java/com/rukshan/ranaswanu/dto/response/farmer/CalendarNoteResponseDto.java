package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarNoteResponseDto {
    private LocalDate date; // serialised as "2026-10-02"
    private String note;    // "" when the day has no note
}
