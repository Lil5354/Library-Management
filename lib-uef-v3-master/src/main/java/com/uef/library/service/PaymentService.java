// PATH: src/main/java/com/uef/library/service/PaymentService.java
package com.uef.library.service;

import com.uef.library.dto.PaymentResponseDTO;

public interface PaymentService {
    /**
     * Tạo yêu cầu thanh toán và trả về thông tin mã QR.
     * @param penaltyId ID của khoản phạt cần thanh toán.
     * @return DTO chứa thông tin mã QR.
     * @throws Exception nếu có lỗi xảy ra.
     */
    PaymentResponseDTO createPaymentRequest(Long penaltyId) throws Exception;

    /**
     * Xử lý callback (webhook) từ cổng thanh toán.
     * Trong dự án này, chúng ta sẽ không có webhook thật,
     * nhưng phương thức này mô phỏng việc nhận và xử lý tín hiệu.
     * @param penaltyId ID của khoản phạt đã được thanh toán thành công.
     */
    void handleSuccessfulPayment(Long penaltyId) throws Exception;
}