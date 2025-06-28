package com.uef.library.config;

import com.uef.library.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBy(String keyword, Long categoryId, String availability,
                                               Integer startYear, Integer endYear, String startsWith) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // 1. Lọc theo từ khóa
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("author")), likePattern)
                ));
            }

            // 2. Lọc theo thể loại
            if (categoryId != null && categoryId > 0) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // 3. Lọc theo trạng thái còn/hết sách
            if (StringUtils.hasText(availability)) {
                if ("available".equalsIgnoreCase(availability)) {
                    predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
                } else if ("borrowed".equalsIgnoreCase(availability)) {
                    predicates.add(cb.equal(root.get("availableCopies"), 0));
                }
            }

            // 4. Lọc theo năm bắt đầu
            if (startYear != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publicationYear"), startYear));
            }

            // 5. Lọc theo năm kết thúc
            if (endYear != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publicationYear"), endYear));
            }

            // 6. Lọc theo chữ cái đầu của tên sách
            if (StringUtils.hasText(startsWith)) {
                predicates.add(cb.like(cb.lower(root.get("title")), startsWith.toLowerCase() + "%"));
            }
            // Sắp xếp kết quả theo tên sách (title) tăng dần
            query.orderBy(cb.asc(root.get("title")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Book> filterByIn(String keyword, Long categoryId, String availability,
                                               Integer startYear, Integer endYear, String startsWith, Double minRating) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // 1. Lọc theo từ khóa
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("author")), likePattern)
                ));
            }

            // 2. Lọc theo thể loại
            if (categoryId != null && categoryId > 0) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // 3. Lọc theo trạng thái còn/hết sách
            if (StringUtils.hasText(availability)) {
                if ("available".equalsIgnoreCase(availability)) {
                    predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
                } else if ("borrowed".equalsIgnoreCase(availability)) {
                    predicates.add(cb.equal(root.get("availableCopies"), 0));
                }
            }

            // 4. Lọc theo năm bắt đầu
            if (startYear != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publicationYear"), startYear));
            }

            // 5. Lọc theo năm kết thúc
            if (endYear != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publicationYear"), endYear));
            }

            // 6. Lọc theo chữ cái đầu của tên sách
            if (StringUtils.hasText(startsWith)) {
                predicates.add(cb.like(cb.lower(root.get("title")), startsWith.toLowerCase() + "%"));
            }
            // 7. THÊM LOGIC LỌC THEO ĐÁNH GIÁ TỐI THIỂU ===

            if (minRating != null && minRating > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            // Sắp xếp kết quả theo tên sách (title) tăng dần
            query.orderBy(cb.asc(root.get("title")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}