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

    /**
     * === PHƯƠNG THỨC BỊ THIẾU GÂY LỖI ===
     * Tìm kiếm một khoản phạt dựa trên ID của mục mượn (LoanItem) và trạng thái.
     * Spring Data JPA sẽ tự động tạo câu lệnh query dựa trên tên của phương thức này.
     *
     * @param loanItemId ID của LoanItem
     * @param status Trạng thái của phí phạt (UNPAID, PAID, WAIVED)
     * @return một Optional chứa PenaltyFee nếu tìm thấy
     */
    Optional<PenaltyFee> findByLoanItemIdAndStatus(Long loanItemId, PenaltyStatus status);

    /**
     * Phương thức để lọc phí phạt theo trạng thái (dùng cho trang quản lý).
     * @param status Trạng thái cần lọc
     * @param pageable Thông tin phân trang
     * @return Một trang các đối tượng PenaltyFee
     */
    Page<PenaltyFee> findByStatus(PenaltyStatus status, Pageable pageable);

    /**
     * Phương thức cho chức năng báo cáo tài chính phí phạt.
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách các map chứa thông tin báo cáo
     */
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