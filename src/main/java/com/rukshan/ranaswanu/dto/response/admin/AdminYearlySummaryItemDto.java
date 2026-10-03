package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminYearlySummaryItemDto {
    private int year;
    private long newUsers;
    private long newProducts;
    private long supportMessages;
}
