// PATH: src/main/java/com/uef/library/controller/api/PaymentController.java
package com.uef.library.controller.api;

import com.uef.library.dto.PaymentResponseDTO;
import com.uef.library.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Endpoint để frontend gọi khi muốn bắt đầu thanh toán QR cho một khoản phạt.
     */
    @PostMapping("/create-qr/{penaltyId}")
    public ResponseEntity<?> createQrPayment(@PathVariable Long penaltyId) {
        try {
            PaymentResponseDTO response = paymentService.createPaymentRequest(penaltyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}