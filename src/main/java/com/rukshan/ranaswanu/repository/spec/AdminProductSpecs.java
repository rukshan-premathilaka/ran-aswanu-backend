package com.rukshan.ranaswanu.repository.spec;

import com.rukshan.ranaswanu.entities.ProductListing;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.service.admin.AdminQueryUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AdminProductSpecs {

    private AdminProductSpecs() {
    }

    // status: null | PUBLISHED | DRAFT | DISABLED (already validated by the service)
    public static Specification<ProductListing> filter(String q, String category, String status,
                                                       Long farmerId, Instant from, Instant toExclusive) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();

            if (AdminQueryUtil.hasText(q)) {
                String pattern = AdminQueryUtil.likePattern(q);
                Join<ProductListing, User> farmer = root.join("user", JoinType.LEFT);
                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(root.<String>get("productName")), pattern, '\\'));
                any.add(cb.like(cb.lower(root.<String>get("category")), pattern, '\\'));
                any.add(cb.like(cb.lower(farmer.<String>get("name")), pattern, '\\'));
                any.add(cb.like(cb.lower(farmer.<String>get("email")), pattern, '\\'));
                if (q.trim().matches("\\d{1,18}")) {
                    any.add(cb.equal(root.<Long>get("id"), Long.parseLong(q.trim())));
                }
                all.add(cb.or(any.toArray(new Predicate[0])));
            }
            if (AdminQueryUtil.hasText(category)) {
                all.add(cb.equal(cb.lower(root.<String>get("category")), category.trim().toLowerCase()));
            }
            if (status != null) {
                switch (status) {
                    case "PUBLISHED" -> {
                        all.add(cb.isTrue(root.<Boolean>get("listingStatus")));
                        all.add(cb.isFalse(root.<Boolean>get("adminDisabled")));
                    }
                    case "DRAFT" -> {
                        all.add(cb.isFalse(root.<Boolean>get("listingStatus")));
                        all.add(cb.isFalse(root.<Boolean>get("adminDisabled")));
                    }
                    case "DISABLED" -> all.add(cb.isTrue(root.<Boolean>get("adminDisabled")));
                    default -> { }
                }
            }
            if (farmerId != null) {
                all.add(cb.equal(root.get("user").<Long>get("id"), farmerId));
            }
            if (from != null) {
                all.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), from));
            }
            if (toExclusive != null) {
                all.add(cb.lessThan(root.<Instant>get("createdAt"), toExclusive));
            }
            return cb.and(all.toArray(new Predicate[0]));
        };
    }
}
