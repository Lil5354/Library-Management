package com.uef.library.service;

import com.uef.library.dto.ReviewDTO;
import com.uef.library.model.Book;
import com.uef.library.model.Review;
import com.uef.library.model.User;
import com.uef.library.repository.BookRepository;
import com.uef.library.repository.LoanItemRepository;
import com.uef.library.repository.ReviewRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final LoanItemRepository loanItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Review createReview(ReviewDTO reviewDto, String username) throws Exception {
        // 1. Lấy các đối tượng cần thiết từ database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng. Vui lòng đăng nhập lại."));

        Book book = bookRepository.findById(reviewDto.getBookId())
                .orElseThrow(() -> new Exception("Không tìm thấy sách để đánh giá."));

        // 2. Kiểm tra các ràng buộc nghiệp vụ
        // Ràng buộc 1: Người dùng phải từng mượn và đã trả sách này.
        // Bạn cần đảm bảo đã có phương thức này trong LoanItemRepository
        if (!loanItemRepository.existsByBookAndBookLoan_UserAndStatus(book, user, "RETURNED")) {
            throw new Exception("Bạn chỉ có thể đánh giá những cuốn sách bạn đã mượn và trả thành công.");
        }

        // Ràng buộc 2: Người dùng chưa từng đánh giá sách này trước đây.
        if (reviewRepository.existsByBook_IdAndUser_UserId(book.getId(), user.getUserId())) {
            throw new Exception("Bạn đã đánh giá cuốn sách này rồi.");
        }

        // 3. Nếu tất cả điều kiện hợp lệ, tạo và lưu đánh giá mới
        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review savedReview = reviewRepository.save(review);

        // 4. Cập nhật lại điểm trung bình và số lượt đánh giá cho cuốn sách
        updateBookAverageRating(book);

        return savedReview;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBook_IdOrderByReviewDateDesc(bookId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Phương thức nội bộ để tính toán và cập nhật lại thông tin đánh giá của sách.
     * @param book Cuốn sách cần cập nhật.
     */
    private void updateBookAverageRating(Book book) {
        List<Review> reviews = reviewRepository.findByBook_IdOrderByReviewDateDesc(book.getId());

        if (reviews.isEmpty()) {
            book.setAverageRating(0.0);
            book.setReviewCount(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            // Làm tròn đến 1 chữ số thập phân
            book.setAverageRating(Math.round(average * 10.0) / 10.0);
            book.setReviewCount(reviews.size());
        }

        bookRepository.save(book);
    }

    /**
     * Phương thức nội bộ để chuyển đổi từ Review Entity sang ReviewDTO.
     * @param review Đối tượng Review Entity.
     * @return Đối tượng ReviewDTO chứa thông tin cần thiết cho giao diện.
     */
    private ReviewDTO convertToDto(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewerName(review.getUser().getUserDetail().getFullName());

        // Định dạng lại ngày tháng cho đẹp hơn
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm");
        dto.setReviewDate(review.getReviewDate().format(formatter));

        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedBook(String userId, Long bookId) {
        return reviewRepository.existsByBook_IdAndUser_UserId(bookId, userId);
    }
}