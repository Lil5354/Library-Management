package com.uef.library.config;

import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * << FILE HỢP NHẤT - ĐÃ SỬA LỖI >>
 * Chứa nhiều phương thức lọc khác nhau để phục vụ cho các trang quản lý riêng biệt.
 */
public class UserSpecification {

    /**
     * Dùng cho trang quản lý Độc giả của Thủ thư (StaffController).
     * Hỗ trợ lọc theo trạng thái thẻ thành viên (active, locked, expired).
     */
    public static Specification<User> filterReaders(String keyword, String role, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<User, UserDetail> userDetailJoin = root.join("userDetail", JoinType.LEFT);

            if (StringUtils.hasText(role)) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (StringUtils.hasText(status)) {
                switch (status.toLowerCase()) {
                    case "active":
                        predicates.add(criteriaBuilder.isTrue(root.get("status")));
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.isNull(userDetailJoin.get("membershipExpiryDate")),
                                criteriaBuilder.greaterThanOrEqualTo(userDetailJoin.get("membershipExpiryDate"), LocalDate.now())
                        ));
                        break;
                    case "locked":
                        predicates.add(criteriaBuilder.isFalse(root.get("status")));
                        break;
                    case "expired":
                        predicates.add(criteriaBuilder.isTrue(root.get("status")));
                        predicates.add(criteriaBuilder.lessThan(userDetailJoin.get("membershipExpiryDate"), LocalDate.now()));
                        break;
                }
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("userId")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userDetailJoin.get("fullName")), likePattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Dùng cho trang quản lý User chung của Admin (AdminController).
     * Hỗ trợ lọc theo trạng thái tài khoản (true/false).
     */
    public static Specification<User> filterGeneric(String keyword, String role, Boolean status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                Join<User, UserDetail> userDetailJoin = root.join("userDetail", JoinType.LEFT);
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likePattern),
                        cb.like(cb.lower(root.get("userId")), likePattern),
                        cb.like(cb.lower(userDetailJoin.get("fullName")), likePattern),
                        cb.like(cb.lower(userDetailJoin.get("email")), likePattern),
                        cb.like(cb.lower(userDetailJoin.get("phone")), likePattern)
                ));
            }

            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), role.toUpperCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
