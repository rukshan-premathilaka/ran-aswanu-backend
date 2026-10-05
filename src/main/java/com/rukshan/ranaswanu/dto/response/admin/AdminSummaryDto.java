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
        private List<RoleCount> byRole;   // includes UNASSIGNED for users who never picked a role
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
        private long published;       // farmer published and not admin-disabled
        private long draft;           // not published and not admin-disabled
        private long adminDisabled;   // published + draft + adminDisabled = total
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
