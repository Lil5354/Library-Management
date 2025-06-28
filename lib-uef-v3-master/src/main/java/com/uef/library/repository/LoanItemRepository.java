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

    @Query("SELECT li FROM LoanItem li WHERE li.bookLoan.user.userId = :userId AND li.book.isbn = :bookIsbn AND li.status = 'BORROWED'")
    Optional<LoanItem> findActiveLoanItemByUserIdAndBookIsbn(@Param("userId") String userId, @Param("bookIsbn") String bookIsbn);

    Optional<LoanItem> findFirstByBookIsbnAndStatus(String bookIsbn, String status);

    @Query("SELECT li FROM LoanItem li WHERE li.book.isbn = :bookIsbn AND li.status = 'BORROWED'")
    List<LoanItem> findAllActiveLoanItemsByBookIsbn(@Param("bookIsbn") String bookIsbn);

    List<LoanItem> findByBookLoan_UserAndStatus(User user, String status);

    @Query("SELECT COUNT(li) FROM LoanItem li WHERE li.bookLoan.user = :user AND li.status = 'BORROWED'")
    long countActiveLoansByUser(@Param("user") User user);

    List<LoanItem> findByBookLoanId(Long bookLoanId);

    List<LoanItem> findByBookLoan_UserAndStatusIn(User user, List<String> statuses);

    boolean existsByBookAndBookLoan_UserAndStatusIn(Book book, User user, List<String> statuses);

    @Query("SELECT li FROM LoanItem li WHERE li.bookLoan.user = :user ORDER BY li.bookLoan.borrowDate DESC")
    Page<LoanItem> findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(@Param("user") User user, Pageable pageable);

    // BỔ SUNG: Đếm số lượng LoanItem theo trạng thái
    long countByStatusIn(List<String> statuses);

    // BỔ SUNG: Đếm số lượng LoanItem đang quá hạn
    // Một LoanItem quá hạn khi status là 'BORROWED' và ngày hiện tại ĐÃ QUA dueDate của BookLoan
    @Query("SELECT COUNT(li) FROM LoanItem li JOIN li.bookLoan bl WHERE li.status = 'BORROWED' AND bl.dueDate < CURRENT_DATE")
    long countOverdueLoans();

    // BỔ SUNG: Đếm số lượng LoanItem được trả trong khoảng thời gian
    // ReturnDate là LocalDateTime, cần dùng BETWEEN trên LocalDateTime
    @Query("SELECT COUNT(li) FROM LoanItem li WHERE li.returnDate BETWEEN :startOfDay AND :endOfDay AND li.status = 'RETURNED'")
    long countByReturnDateBetween(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);

    boolean existsByBookAndBookLoan_UserAndStatus(Book book, User user, String status);

}