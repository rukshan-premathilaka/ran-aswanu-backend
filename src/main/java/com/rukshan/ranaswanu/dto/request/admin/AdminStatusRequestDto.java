package com.rukshan.ranaswanu.dto.request.admin;

import lombok.Data;

@Data
public class AdminStatusRequestDto {

    // true = enabled / published, false = disabled / unpublished.
    // Checked in the controller so a missing value returns a clear 400 (it must not silently become false).
    private Boolean active;
}
