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
    private List<Item> months;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private int month;
        private String label;
        private long newUsers;
        private long newProducts;
    }
}
