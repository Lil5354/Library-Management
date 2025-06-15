package com.uef.library.config;

import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterBy(String keyword, String role, Boolean status) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Quan trọng: Để tránh lỗi N+1 hoặc lazy loading khi join và count,
            // khi query là count query, không nên add join.
            // Chỉ add join khi là select query (không phải count).
            // Tuy nhiên, PageableExecutionUtils của Spring Data JPA thường xử lý việc này.
            // Nhưng để tìm kiếm trên các trường của UserDetail, chúng ta cần join.

            if (StringUtils.hasText(keyword)) {
                // Join với UserDetail để tìm kiếm trong fullName, email
                // Dùng LEFT JOIN để vẫn lấy user ngay cả khi họ chưa có UserDetail
                Join<User, UserDetail> userDetailJoin = root.join("userDetail", JoinType.LEFT);

                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likePattern),
                        cb.like(cb.lower(root.get("userId")), likePattern), // Thêm tìm theo ID nếu muốn
                        cb.like(cb.lower(userDetailJoin.get("fullName")), likePattern),
                        cb.like(cb.lower(userDetailJoin.get("email")), likePattern)
                        // cb.like(cb.lower(userDetailJoin.get("phone")), likePattern) // Bỏ comment nếu muốn tìm theo SĐT
                ));
            }

            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), role.toUpperCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Để tránh duplicate users khi join với collection (nếu có), dùng distinct
            // query.distinct(true); // Cần thiết nếu join tới ToMany, ở đây là ToOne nên có thể không quá quan trọng

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}