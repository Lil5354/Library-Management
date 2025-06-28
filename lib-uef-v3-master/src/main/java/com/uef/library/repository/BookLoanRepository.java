package com.uef.library.repository;

import com.uef.library.dto.TopBookDTO;
import com.uef.library.model.BookLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT bl FROM BookLoan bl WHERE bl.status = 'ACTIVE'")
    Page<BookLoan> findAllActiveLoans(Pageable pageable);

    @Query("SELECT DISTINCT bl FROM BookLoan bl JOIN bl.user u JOIN u.userDetail ud " + // <-- THÊM "JOIN u.userDetail ud"
            "WHERE bl.status = 'ACTIVE' AND " +
            "(LOWER(u.userId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))") // <-- SỬA THÀNH "ud.fullName"
    Page<BookLoan> findActiveLoansBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT bl FROM BookLoan bl JOIN bl.loanItems li " +
            "WHERE bl.status = 'ACTIVE' AND bl.dueDate < :currentDate AND li.status = 'BORROWED'")
    List<BookLoan> findActiveOverdueBookLoans(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT new com.uef.library.dto.TopBookDTO(li.book.title, COUNT(li.id)) " +
            "FROM LoanItem li JOIN li.bookLoan bl " +
            "WHERE YEAR(bl.borrowDate) = :year AND MONTH(bl.borrowDate) = :month " +
            "GROUP BY li.book.title " +
            "ORDER BY COUNT(li.id) DESC " +
            "LIMIT 5")
    List<TopBookDTO> findTopBorrowedBooks(@Param("year") int year, @Param("month") int month);

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

    @Query("SELECT new map(li.book.title as bookTitle, bl.user.userDetail.fullName as readerName, bl.dueDate as dueDate, bl.borrowDate as borrowDate) " +
            "FROM LoanItem li JOIN li.bookLoan bl " +
            "WHERE li.status = 'BORROWED' AND bl.dueDate < :currentDate " +
            "ORDER BY bl.dueDate ASC")
    List<Map<String, Object>> findOverdueLoanItems(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT new map(u.userDetail.fullName as readerName, COUNT(li.id) as borrowCount) " +
            "FROM LoanItem li JOIN li.bookLoan bl JOIN bl.user u " +
            "WHERE bl.borrowDate >= :startDate AND bl.borrowDate <= :endDate " +
            "GROUP BY u.userDetail.fullName " +
            "ORDER BY COUNT(li.id) DESC")
    List<Map<String, Object>> countLoansByUserBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT CAST(bl.borrow_date AS DATE) as borrowDay, COUNT(bl.id) as borrowCount " +
            "FROM book_loans bl " +
            "WHERE bl.borrow_date BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(bl.borrow_date AS DATE) " +
            "ORDER BY CAST(bl.borrow_date AS DATE) ASC",
            nativeQuery = true)
    List<Map<String, Object>> countBookLoansPerDayBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByBorrowDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);


}