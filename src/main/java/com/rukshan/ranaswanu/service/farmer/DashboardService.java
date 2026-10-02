package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.response.farmer.DashboardSummaryDto;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

@Service
public class DashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private FieldPlotRepository fieldPlotRepository;
    @Autowired private CropRepository cropRepository;
    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private LiveStockRepository liveStockRepository;
    @Autowired private ExpensRepository expensRepository;

    public DashboardSummaryDto getSummary(String farmerEmail) {
        User farmer = userRepository.findByEmail(farmerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + farmerEmail));
        Long id = farmer.getId();

        // "This month" = from the 1st 00:00 (UTC) to the 1st of next month
        YearMonth thisMonth = YearMonth.now(ZoneOffset.UTC);
        Instant start = thisMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = thisMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return DashboardSummaryDto.builder()
                .totalFieldPlots(fieldPlotRepository.countByUserId(id))
                .totalCrops(cropRepository.countByUserId(id))
                .activeListings(productListingRepository.countByUserIdAndListingStatusTrue(id))
                .pendingOrders(countPendingOrders(id))
                .totalLivestock(liveStockRepository.sumAmountByUserId(id))
                .monthExpenses(expensRepository.sumAmountBetween(id, start, end))
                .build();
    }

    // TODO (Step 7): orders are not real yet (Order.orderStatus is still a Boolean).
    // After the order migration, replace this with a repository query:
    //   count of orders with status 'PENDING' that contain this farmer's products.
    private long countPendingOrders(Long farmerId) {
        return 0;
    }
}
