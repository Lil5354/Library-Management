// PATH: src/main/java/com/uef/library/service/PenaltyFeeService.java
package com.uef.library.service;

import com.uef.library.dto.PenaltyFeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PenaltyFeeService {
    // Lấy danh sách phí phạt có phân trang, có thể lọc theo trạng thái
    Page<PenaltyFeeDTO> getAllPenaltyFees(String status, Pageable pageable);

    // Đánh dấu phí phạt là đã thu
    PenaltyFeeDTO markPenaltyAsPaid(Long penaltyId, String staffUserId) throws Exception;

    // Miễn giảm phí phạt
    PenaltyFeeDTO waivePenaltyFee(Long penaltyId, String reason, String staffUserId) throws Exception;

    // Lấy chi tiết một khoản phí phạt
    PenaltyFeeDTO getPenaltyFeeById(Long penaltyId) throws Exception;

    // <<< CHỈNH SỬA KIỂU TRẢ VỀ CỦA PHƯƠNG THỨC NÀY >>>
    // Tính toán và tạo phí phạt cho một lượt mượn (được gọi nội bộ từ BorrowingService)
    PenaltyFeeDTO createPenaltyFeeForOverdueBorrowing(Long borrowingId, int overdueDays, Double penaltyAmount) throws Exception;

    // Kiểm tra xem một lượt mượn đã có phí phạt UNPAID chưa
    boolean hasUnpaidPenalty(Long borrowingId);
}