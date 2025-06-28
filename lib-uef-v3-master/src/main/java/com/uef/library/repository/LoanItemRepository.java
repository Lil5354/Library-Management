package com.uef.library.repository;

import com.uef.library.model.Book;
import com.uef.library.model.LoanItem;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {

    // <<< THAY ĐỔI: Thêm phương thức mới để tìm sách trong một phiếu mượn cụ thể >>>
    @Query("SELECT li FROM LoanItem li " +
            "WHERE li.bookLoan.id = :loanId " +
            "AND li.book.isbn = :isbn " +
            "AND li.status IN ('BORROWED', 'OVERDUE')")
    Optional<LoanItem> findActiveLoanItemByLoanIdAndBookIsbn(@Param("loanId") Long loanId, @Param("isbn") String isbn);

    // --- CÁC PHƯƠNG THỨC CŨ (giữ lại nếu cần) ---
    List<LoanItem> findByBookLoanId(Long loanId);

    // Câu truy vấn này không còn an toàn cho chức năng trả nhiều sách, nhưng có thể cần cho nơi khác
    @Query("SELECT li FROM LoanItem li " +
            "WHERE li.bookLoan.user.userId = :userId " +
            "AND li.book.isbn = :bookIsbn " +
            "AND li.status IN ('BORROWED', 'OVERDUE')")
    Optional<LoanItem> findActiveLoanItemByUserIdAndBookIsbn(@Param("userId") String userId, @Param("bookIsbn") String bookIsbn);

    Optional<LoanItem> findFirstByBookIsbnAndStatus(String bookIsbn, String status);

    @Query("SELECT count(li) FROM LoanItem li WHERE li.bookLoan.user = :user AND li.status IN ('BORROWED', 'OVERDUE')")
    long countActiveLoansByUser(@Param("user") User user);

    List<LoanItem> findByBookLoan_UserAndStatusIn(User user, List<String> statuses);

    List<LoanItem> findByBookLoan_UserAndStatus(User user, String status);

    Page<LoanItem> findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(User user, Pageable pageable);

    boolean existsByBookAndBookLoan_UserAndStatus(Book book, User user, String status);

    boolean existsByBookAndBookLoan_UserAndStatusIn(Book book, User user, List<String> statuses);

}