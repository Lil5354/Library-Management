package com.uef.library.service;

import com.uef.library.dto.PaymentResponseDTO;
import com.uef.library.model.PenaltyFee;
import com.uef.library.model.PenaltyStatus;
import com.uef.library.repository.PenaltyFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PenaltyFeeRepository penaltyFeeRepository;
    private final PenaltyFeeService penaltyFeeService; // Dùng để gọi logic markAsPaid đã có

    // --- Lấy các giá trị từ file application.properties ---
    @Value("${vietqr.bank.bin}")
    private String bankBin;

    @Value("${vietqr.account.no}")
    private String accountNo;

    @Value("${vietqr.account.name}")
    private String accountName;

    @Value("${vietqr.template}")
    private String template;
    // --------------------------------------------------------

    @Override
    public PaymentResponseDTO createPaymentRequest(Long penaltyId) throws Exception {
        PenaltyFee penaltyFee = penaltyFeeRepository.findById(penaltyId)
                .orElseThrow(() -> new Exception("Không tìm thấy khoản phạt với ID: " + penaltyId));

        if (penaltyFee.getStatus() != PenaltyStatus.UNPAID) {
            throw new Exception("Khoản phạt này không ở trạng thái 'Chưa thanh toán'.");
        }

        double amount = penaltyFee.getPenaltyAmount();
        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);

        // --- TẠO MÃ VIETQR THAY VÌ MÃ DEMO ---

        // 1. Tạo nội dung chuyển khoản để dễ đối soát
        // Ví dụ: "Thanh toan phat 123"
        String paymentDescription = "Thanh toan phat " + penaltyId;

        // 2. Mã hóa các tham số để đảm bảo URL hợp lệ
        String encodedAccountName = URLEncoder.encode(this.accountName, StandardCharsets.UTF_8.toString());
        String encodedDescription = URLEncoder.encode(paymentDescription, StandardCharsets.UTF_8.toString());

        // 3. Xây dựng URL tạo mã VietQR theo chuẩn của img.vietqr.io
        String qrImageUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                this.bankBin,
                this.accountNo,
                this.template,
                (int) amount, // VietQR yêu cầu số tiền là số nguyên
                encodedDescription,
                encodedAccountName
        );

        return new PaymentResponseDTO(qrImageUrl, penaltyId, formattedAmount);
    }

    @Override
    public void handleSuccessfulPayment(Long penaltyId) throws Exception {
        // Hàm này sẽ được gọi bởi webhook hoặc một cơ chế giả lập webhook
        // Trong trường hợp VietQR, việc đối soát thường được làm thủ công hoặc qua sao kê ngân hàng
        // Tuy nhiên, logic polling ở frontend vẫn hoạt động để chờ thủ thư xác nhận.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String staffUserId = authentication.getName(); // Giả sử userId là username của thủ thư

        // Gọi lại service đã có để xử lý logic nghiệp vụ
        penaltyFeeService.markPenaltyAsPaid(penaltyId, staffUserId);
    }
}
