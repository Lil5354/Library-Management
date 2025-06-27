package com.uef.library.repository;

import com.uef.library.dto.TopBookDTO;
import com.uef.library.model.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    // Lấy tất cả các phiếu mượn đang hoạt động (chưa hoàn tất)
    @Query("SELECT bl FROM BookLoan bl WHERE bl.status = 'ACTIVE' ORDER BY bl.dueDate ASC")
    List<BookLoan> findActiveBookLoans();

    // Lấy tất cả các phiếu mượn đang HOẠT ĐỘNG và có ÍT NHẤT MỘT SÁCH QUÁ HẠN
    // Tức là dueDate của phiếu mượn đã qua ngày hiện tại
    @Query("SELECT DISTINCT bl FROM BookLoan bl JOIN bl.loanItems li " +
            "WHERE bl.status = 'ACTIVE' AND bl.dueDate < :currentDate AND li.status = 'BORROWED'")
    List<BookLoan> findActiveOverdueBookLoans(@Param("currentDate") LocalDate currentDate);

    // --- Phương thức cho Widget "Top sách mượn nhiều nhất" ---
    @Query("SELECT new com.uef.library.dto.TopBookDTO(li.book.title, COUNT(li.id)) " +
            "FROM LoanItem li JOIN li.bookLoan bl " +
            "WHERE YEAR(bl.borrowDate) = :year AND MONTH(bl.borrowDate) = :month " +
            "GROUP BY li.book.title " +
            "ORDER BY COUNT(li.id) DESC " +
            "LIMIT 5")
    List<TopBookDTO> findTopBorrowedBooks(@Param("year") int year, @Param("month") int month);

    // --- Phương thức cho Báo cáo tùy chỉnh "Lượt mượn/trả" ---
    @Query("SELECT new map(" +
            "li.book.title as bookTitle, " +
            "bl.user.userDetail.fullName as readerName, " +
            "bl.borrowDate as borrowDate, " +
            "li.returnDate as returnDate, " +
            "CASE WHEN li.status = 'BORROWED' THEN 'Đang mượn' ELSE 'Đã trả' END as status) " +
            "FROM LoanItem li JOIN li.bookLoan bl " +
            "WHERE bl.borrowDate >= :startDate AND bl.borrowDate <= :endDate " +
            "ORDER BY bl.borrowDate DESC")
    List<Map<String, Object>> findLoanItemsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // --- Phương thức cho Báo cáo "Sách quá hạn" ---
    @Query("SELECT new map(li.book.title as bookTitle, bl.user.userDetail.fullName as readerName, bl.dueDate as dueDate, bl.borrowDate as borrowDate) " +
            "FROM LoanItem li JOIN li.bookLoan bl " +
            "WHERE li.status = 'BORROWED' AND bl.dueDate < :currentDate " +
            "ORDER BY bl.dueDate ASC")
    List<Map<String, Object>> findOverdueLoanItems(@Param("currentDate") LocalDate currentDate);

    // --- Phương thức cho Báo cáo "Hoạt động độc giả" ---
    @Query("SELECT new map(u.userDetail.fullName as readerName, COUNT(li.id) as borrowCount) " +
            "FROM LoanItem li JOIN li.bookLoan bl JOIN bl.user u " +
            "WHERE bl.borrowDate >= :startDate AND bl.borrowDate <= :endDate " +
            "GROUP BY u.userDetail.fullName " +
            "ORDER BY COUNT(li.id) DESC")
    List<Map<String, Object>> countLoansByUserBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // --- Phương thức cho Biểu đồ (đếm số phiếu mượn theo ngày) ---
    @Query(value = "SELECT CAST(bl.borrow_date AS DATE) as borrowDay, COUNT(bl.id) as borrowCount " +
            "FROM book_loans bl " +
            "WHERE bl.borrow_date BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(bl.borrow_date AS DATE) " +
            "ORDER BY CAST(bl.borrow_date AS DATE) ASC",
            nativeQuery = true)
    List<Map<String, Object>> countBookLoansPerDayBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}