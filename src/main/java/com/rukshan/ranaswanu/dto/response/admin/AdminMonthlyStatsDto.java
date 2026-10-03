package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonthlyStatsDto {
    private int year;
    private long totalNewUsers;
    private long totalNewProducts;
    private List<Item> months;   // always 12 items, empty months have 0

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private int month;       // 1 to 12
        private String label;    // Jan, Feb ...
        private long newUsers;
        private long newProducts;
    }
}
