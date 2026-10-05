package com.rukshan.ranaswanu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {

    @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;
}
