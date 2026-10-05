package com.rukshan.ranaswanu.dto.request.farmer;

import lombok.Data;

@Data
public class ProductStatusRequestDto {

    // Checked in ProductController so a missing value returns { "error": "published is required (true or false)" }
    private Boolean published;
}
