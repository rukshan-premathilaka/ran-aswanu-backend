package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonthlySummaryItemDto {
    private int year;
    private int month;        // 1 = January ... 12 = December
    private String monthName; // "Jan", "Feb", ...
    private long newUsers;
    private long newProducts;
    private long supportMessages;
}
