package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalendarNoteRequestDto {

    @NotBlank(message = "Note is required")
    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;
}
