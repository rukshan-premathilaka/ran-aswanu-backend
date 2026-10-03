package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryDto {
    private long totalUsers;
    private long activeUsers;
    private long disabledUsers;
    private long farmerCount;
    private long buyerCount;
    private long transportCount;
    private long adminCount;

    private long totalProducts;
    private long activeProducts;
    private long disabledProducts;

    private long totalSupportMessages;
}
