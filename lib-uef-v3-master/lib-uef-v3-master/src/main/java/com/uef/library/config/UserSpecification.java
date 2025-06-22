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