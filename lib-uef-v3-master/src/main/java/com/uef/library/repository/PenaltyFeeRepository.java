package com.uef.library.repository;

import com.uef.library.model.PenaltyFee;
import com.uef.library.model.PenaltyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PenaltyFeeRepository extends JpaRepository<PenaltyFee, Long> {

    Optional<PenaltyFee> findByLoanItemIdAndStatus(Long loanItemId, PenaltyStatus status);

    Page<PenaltyFee> findByStatus(PenaltyStatus status, Pageable pageable);

    // ===== PHƯƠNG THỨC MỚI CHO TÌM KIẾM =====
    @Query("SELECT pf FROM PenaltyFee pf JOIN pf.user u " +
            "WHERE LOWER(u.userId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.userDetail.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PenaltyFee> findAllBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT pf FROM PenaltyFee pf JOIN pf.user u " +
            "WHERE pf.status = :status AND " +
            "(LOWER(u.userId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.userDetail.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PenaltyFee> findByStatusAndSearchTerm(@Param("status") PenaltyStatus status, @Param("searchTerm") String searchTerm, Pageable pageable);
    // =========================================


    @Query("SELECT new map(" +
            "pf.user.userDetail.fullName as readerName, " +
            "pf.book.title as bookTitle, " +
            "pf.penaltyAmount as amount, " +
            "pf.status as status, " +
            "pf.createdAt as createdAt) " +
            "FROM PenaltyFee pf " +
            "WHERE pf.createdAt >= :startDate AND pf.createdAt <= :endDate " +
            "ORDER BY pf.createdAt DESC")
    List<Map<String, Object>> findPenaltiesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}