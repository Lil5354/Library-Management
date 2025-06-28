package com.uef.library.service;

import com.uef.library.dto.ReviewDTO;
import com.uef.library.model.Review;

import java.util.List;

public interface ReviewService {

    /**
     * Tạo một đánh giá mới cho một cuốn sách.
     *
     * @param reviewDto Đối tượng chứa thông tin đánh giá từ người dùng (sao, cảm nghĩ, bookId).
     * @param username  Tên đăng nhập của người dùng thực hiện đánh giá.
     * @return Đối tượng Review đã được lưu.
     * @throws Exception nếu người dùng không đủ điều kiện đánh giá hoặc có lỗi xảy ra.
     */
    Review createReview(ReviewDTO reviewDto, String username) throws Exception;

    /**
     * Lấy tất cả các đánh giá của một cuốn sách.
     *
     * @param bookId ID của cuốn sách cần lấy đánh giá.
     * @return Danh sách các DTO chứa thông tin đánh giá.
     */
    List<ReviewDTO> getReviewsForBook(Long bookId);
    boolean hasUserReviewedBook(String userId, Long bookId);

}