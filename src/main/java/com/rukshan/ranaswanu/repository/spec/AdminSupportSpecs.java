package com.rukshan.ranaswanu.repository.spec;

import com.rukshan.ranaswanu.entities.SupportMessage;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.service.admin.AdminQueryUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AdminSupportSpecs {

    private AdminSupportSpecs() {
    }

    public static Specification<SupportMessage> filter(String q, Long userId,
                                                       Instant from, Instant toExclusive) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();

            if (AdminQueryUtil.hasText(q)) {
                String pattern = AdminQueryUtil.likePattern(q);
                Join<SupportMessage, User> sender = root.join("user", JoinType.LEFT);
                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(root.<String>get("subject")), pattern, '\\'));
                any.add(cb.like(cb.lower(root.<String>get("message")), pattern, '\\'));
                any.add(cb.like(cb.lower(sender.<String>get("name")), pattern, '\\'));
                any.add(cb.like(cb.lower(sender.<String>get("email")), pattern, '\\'));
                all.add(cb.or(any.toArray(new Predicate[0])));
            }
            if (userId != null) {
                all.add(cb.equal(root.get("user").<Long>get("id"), userId));
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
