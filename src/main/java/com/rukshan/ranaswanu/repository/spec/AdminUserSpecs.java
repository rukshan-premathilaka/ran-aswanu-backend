package com.rukshan.ranaswanu.repository.spec;

import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.service.admin.AdminQueryUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class AdminUserSpecs {

    private AdminUserSpecs() {
    }

    // q = text typed in the search box (name, email, phone, or the exact user id)
    // role / active / from / toExclusive may be null = not filtered
    public static Specification<User> filter(String q, String role, Boolean active,
                                             Instant from, Instant toExclusive) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();

            if (AdminQueryUtil.hasText(q)) {
                String pattern = AdminQueryUtil.likePattern(q);
                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(root.<String>get("name")), pattern, '\\'));
                any.add(cb.like(cb.lower(root.<String>get("email")), pattern, '\\'));
                any.add(cb.like(cb.lower(root.<String>get("phoneNumber")), pattern, '\\'));
                if (q.trim().matches("\\d{1,18}")) {
                    any.add(cb.equal(root.<Long>get("id"), Long.parseLong(q.trim())));
                }
                all.add(cb.or(any.toArray(new Predicate[0])));
            }
            if (role != null) {
                all.add(cb.equal(root.<String>get("role"), role));
            }
            if (active != null) {
                all.add(cb.equal(root.<Boolean>get("active"), active));
            }
            if (from != null) {
                all.add(cb.greaterThanOrEqualTo(root.<Date>get("createdAt"), Date.from(from)));
            }
            if (toExclusive != null) {
                all.add(cb.lessThan(root.<Date>get("createdAt"), Date.from(toExclusive)));
            }
            return cb.and(all.toArray(new Predicate[0]));
        };
    }
}
