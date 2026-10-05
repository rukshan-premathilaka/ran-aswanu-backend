package com.rukshan.ranaswanu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupportMessageRequestDto {

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must be at most 100 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;
}
