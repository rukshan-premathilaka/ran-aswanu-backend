package com.rukshan.ranaswanu.service.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


public final class AdminQueryUtil {


    public static final ZoneId ZONE = ZoneId.systemDefault();

    public static final int MAX_PAGE_SIZE = 100;

    private AdminQueryUtil() {
    }


    public static Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    public static Instant startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZONE).toInstant();
    }


    public static Instant startOfNextDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay(ZONE).toInstant();
    }

    public static void checkDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'.");
        }
    }


    public static String likePattern(String text) {
        String t = text.trim().toLowerCase();
        t = t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[");
        return "%" + t + "%";
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
