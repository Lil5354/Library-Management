package com.uef.library.repository;
import com.uef.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Kiểm tra xem người dùng đã đánh giá sách này chưa
    boolean existsByBook_IdAndUser_UserId(Long bookId, String userId);

    // Lấy tất cả đánh giá của một sách
    List<Review> findByBook_IdOrderByReviewDateDesc(Long bookId);
}