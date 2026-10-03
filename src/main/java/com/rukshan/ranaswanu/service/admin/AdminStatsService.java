package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminMonthlyStatsDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminSummaryDto;
import com.rukshan.ranaswanu.dto.response.admin.AdminYearlyStatsDto;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.SupportMessageRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AdminStatsService {

    private static final int FIRST_YEAR = 2000;
    private static final int MAX_YEARS = 50;

    @Autowired private UserRepository userRepository;
    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private SupportMessageRepository supportMessageRepository;

    // ---------------- SUMMARY ----------------

    @Transactional(readOnly = true)
    public AdminSummaryDto summary() {
        LocalDate today = LocalDate.now(AdminQueryUtil.ZONE);
        Instant monthStart = at(YearMonth.from(today).atDay(1));
        Instant nextMonthStart = at(YearMonth.from(today).plusMonths(1).atDay(1));
        Instant yearStart = at(LocalDate.of(today.getYear(), 1, 1));
        Instant nextYearStart = at(LocalDate.of(today.getYear() + 1, 1, 1));

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);

        List<AdminSummaryDto.RoleCount> byRole = new ArrayList<>();
        for (String role : List.of("FARMER", "BUYER", "TRANSPORT", "ADMIN")) {
            byRole.add(new AdminSummaryDto.RoleCount(role, userRepository.countByRole(role)));
        }
        byRole.add(new AdminSummaryDto.RoleCount("UNASSIGNED", userRepository.countByRoleIsNull()));

        AdminSummaryDto.UserCounts users = AdminSummaryDto.UserCounts.builder()
                .total(totalUsers)
                .active(activeUsers)
                .disabled(totalUsers - activeUsers)
                .newThisMonth(countUsers(monthStart, nextMonthStart))
                .newThisYear(countUsers(yearStart, nextYearStart))
                .byRole(byRole)
                .build();

        AdminSummaryDto.ProductCounts products = AdminSummaryDto.ProductCounts.builder()
                .total(productListingRepository.count())
                .published(productListingRepository.countByListingStatusTrueAndAdminDisabledFalse())
                .draft(productListingRepository.countByListingStatusFalseAndAdminDisabledFalse())
                .adminDisabled(productListingRepository.countByAdminDisabledTrue())
                .newThisMonth(productListingRepository.countCreatedBetween(monthStart, nextMonthStart))
                .newThisYear(productListingRepository.countCreatedBetween(yearStart, nextYearStart))
                .build();

        AdminSummaryDto.SupportCounts support = AdminSummaryDto.SupportCounts.builder()
                .total(supportMessageRepository.count())
                .thisMonth(supportMessageRepository.countCreatedBetween(monthStart, nextMonthStart))
                .build();

        return AdminSummaryDto.builder().users(users).products(products).supportMessages(support).build();
    }

    // ---------------- MONTHLY ----------------

    @Transactional(readOnly = true)
    public AdminMonthlyStatsDto monthly(Integer yearParam) {
        int currentYear = LocalDate.now(AdminQueryUtil.ZONE).getYear();
        int year = yearParam == null ? currentYear : yearParam;
        if (year < FIRST_YEAR || year > currentYear) {
            throw new IllegalArgumentException("Year must be between " + FIRST_YEAR + " and " + currentYear + ".");
        }

        List<AdminMonthlyStatsDto.Item> months = new ArrayList<>();
        long totalUsers = 0;
        long totalProducts = 0;

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            Instant from = at(ym.atDay(1));
            Instant to = at(ym.plusMonths(1).atDay(1));

            long newUsers = countUsers(from, to);
            long newProducts = productListingRepository.countCreatedBetween(from, to);
            totalUsers += newUsers;
            totalProducts += newProducts;

            months.add(AdminMonthlyStatsDto.Item.builder()
                    .month(m)
                    .label(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .newUsers(newUsers)
                    .newProducts(newProducts)
                    .build());
        }

        return AdminMonthlyStatsDto.builder()
                .year(year)
                .totalNewUsers(totalUsers)
                .totalNewProducts(totalProducts)
                .months(months)
                .build();
    }

    // ---------------- YEARLY ----------------

    @Transactional(readOnly = true)
    public AdminYearlyStatsDto yearly() {
        int currentYear = LocalDate.now(AdminQueryUtil.ZONE).getYear();

        int firstYear = currentYear;
        Date firstUser = userRepository.findEarliestCreatedAt();
        if (firstUser != null) {
            // new Date(getTime()) avoids java.sql.Timestamp / java.sql.Date quirks
            firstYear = Math.min(firstYear, yearOf(new Date(firstUser.getTime()).toInstant()));
        }
        Instant firstProduct = productListingRepository.findEarliestCreatedAt();
        if (firstProduct != null) {
            firstYear = Math.min(firstYear, yearOf(firstProduct));
        }
        firstYear = Math.max(firstYear, Math.max(FIRST_YEAR, currentYear - MAX_YEARS + 1));

        List<AdminYearlyStatsDto.Item> years = new ArrayList<>();
        for (int y = firstYear; y <= currentYear; y++) {
            Instant from = at(LocalDate.of(y, 1, 1));
            Instant to = at(LocalDate.of(y + 1, 1, 1));
            years.add(AdminYearlyStatsDto.Item.builder()
                    .year(y)
                    .newUsers(countUsers(from, to))
                    .newProducts(productListingRepository.countCreatedBetween(from, to))
                    .build());
        }
        return AdminYearlyStatsDto.builder().years(years).build();
    }

    // ---------------- HELPERS ----------------

    private long countUsers(Instant from, Instant to) {
        return userRepository.countCreatedBetween(Date.from(from), Date.from(to));
    }

    private Instant at(LocalDate date) {
        return date.atStartOfDay(AdminQueryUtil.ZONE).toInstant();
    }

    private int yearOf(Instant instant) {
        return instant.atZone(AdminQueryUtil.ZONE).getYear();
    }
}
