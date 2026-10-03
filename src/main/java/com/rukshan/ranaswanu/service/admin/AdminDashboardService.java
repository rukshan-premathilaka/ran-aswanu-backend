package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminDashboardSummaryDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminMonthlySummaryItemDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminYearlySummaryItemDto;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Read-only dashboard. Dates come from the existing created_at columns (counted in UTC).
@Service
public class AdminDashboardService {

    private static final String[] MONTH_NAMES =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Autowired private UserRepository userRepository;
    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private SupportMessageRepository supportMessageRepository;

    // ---------------- CURRENT COUNTS ----------------

    @Transactional(readOnly = true)
    public AdminDashboardSummaryDto getSummary() {
        return AdminDashboardSummaryDto.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByActive(true))
                .disabledUsers(userRepository.countByActive(false))
                .farmerCount(userRepository.countByRole("FARMER"))
                .buyerCount(userRepository.countByRole("BUYER"))
                .transportCount(userRepository.countByRole("TRANSPORT"))
                .adminCount(userRepository.countByRole("ADMIN"))
                .totalProducts(productListingRepository.count())
                .activeProducts(productListingRepository.countByListingStatus(true))
                .disabledProducts(productListingRepository.countByListingStatus(false))
                .totalSupportMessages(supportMessageRepository.count())
                .build();
    }

    // ---------------- MONTHLY (one year, January -> December) ----------------

    @Transactional(readOnly = true)
    public List<AdminMonthlySummaryItemDto> getMonthly(Integer year) {
        int wantedYear = (year != null) ? year : Year.now(ZoneOffset.UTC).getValue();
        if (wantedYear < 2000 || wantedYear > 2100) {
            throw new IllegalArgumentException("Invalid year. Use a value between 2000 and 2100.");
        }

        long[] users = new long[12];
        long[] products = new long[12];
        long[] support = new long[12];

        for (Instant at : userDates()) {
            countMonth(at, wantedYear, users);
        }
        for (Instant at : productListingRepository.findAllCreatedAt()) {
            countMonth(at, wantedYear, products);
        }
        for (Instant at : supportMessageRepository.findAllCreatedAt()) {
            countMonth(at, wantedYear, support);
        }

        List<AdminMonthlySummaryItemDto> result = new ArrayList<>();
        for (int m = 0; m < 12; m++) {
            result.add(AdminMonthlySummaryItemDto.builder()
                    .year(wantedYear)
                    .month(m + 1)
                    .monthName(MONTH_NAMES[m])
                    .newUsers(users[m])
                    .newProducts(products[m])
                    .supportMessages(support[m])
                    .build());
        }
        return result;
    }

    // ---------------- YEARLY (every year found in the database, gaps filled with 0) ----------------

    @Transactional(readOnly = true)
    public List<AdminYearlySummaryItemDto> getYearly() {
        TreeMap<Integer, long[]> byYear = new TreeMap<>(); // [0]=users [1]=products [2]=support

        for (Instant at : userDates()) {
            countYear(at, byYear, 0);
        }
        for (Instant at : productListingRepository.findAllCreatedAt()) {
            countYear(at, byYear, 1);
        }
        for (Instant at : supportMessageRepository.findAllCreatedAt()) {
            countYear(at, byYear, 2);
        }

        List<AdminYearlySummaryItemDto> result = new ArrayList<>();
        if (byYear.isEmpty()) {
            return result;
        }

        int first = byYear.firstKey();
        int last = byYear.lastKey();
        for (int y = first; y <= last; y++) {
            long[] c = byYear.getOrDefault(y, new long[3]);
            result.add(AdminYearlySummaryItemDto.builder()
                    .year(y)
                    .newUsers(c[0])
                    .newProducts(c[1])
                    .supportMessages(c[2])
                    .build());
        }
        return result;
    }

    // ---------------- HELPERS ----------------

    // users.created_at is a java.util.Date; the other two tables use Instant
    private List<Instant> userDates() {
        List<Instant> dates = new ArrayList<>();
        for (Date d : userRepository.findAllCreatedAt()) {
            if (d != null) {
                dates.add(Instant.ofEpochMilli(d.getTime()));
            }
        }
        return dates;
    }

    private void countMonth(Instant at, int year, long[] counters) {
        if (at == null) {
            return;
        }
        var date = at.atZone(ZoneOffset.UTC);
        if (date.getYear() == year) {
            counters[date.getMonthValue() - 1]++;
        }
    }

    private void countYear(Instant at, Map<Integer, long[]> byYear, int index) {
        if (at == null) {
            return;
        }
        int year = at.atZone(ZoneOffset.UTC).getYear();
        byYear.computeIfAbsent(year, k -> new long[3])[index]++;
    }
}
