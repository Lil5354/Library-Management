package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * <<< FILE HỢP NHẤT - ĐÃ SỬA LỖI >>
 * DTO cho kết quả sau khi xử lý trả nhiều sách.
 * Bao gồm cả danh sách trả thành công, các khoản phí phát sinh, và các lỗi gặp phải.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnMultipleResponseDTO {
    /**
     * Danh sách các mục mượn đã được trả thành công.
     */
    private List<LoanItemDTO> returnedItems;

    /**
     * Danh sách các khoản phí phạt mới được tạo ra trong quá trình trả sách.
     */
    private List<PenaltyFeeDTO> penaltyFees;

    /**
     * Danh sách các thông báo lỗi cho những sách không thể xử lý.
     */
    private List<String> errors;
}
