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
public class AdminSummaryDto {

    private UserCounts users;
    private ProductCounts products;
    private SupportCounts supportMessages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCounts {
        private long total;
        private long active;
        private long disabled;
        private long newThisMonth;
        private long newThisYear;
        private List<RoleCount> byRole;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleCount {
        private String role;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCounts {
        private long total;
        private long published;
        private long draft;
        private long adminDisabled;
        private long newThisMonth;
        private long newThisYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportCounts {
        private long total;
        private long thisMonth;
    }
}
