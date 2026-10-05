package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalFieldPlots;
    private long totalCrops;
    private long activeListings;
    private long pendingOrders;
    private long totalLivestock;
    private long monthExpenses;
}
