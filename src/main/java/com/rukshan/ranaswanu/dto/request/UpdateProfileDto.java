package com.rukshan.ranaswanu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {

    @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 255)
    private String address;
}