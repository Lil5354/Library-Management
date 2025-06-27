// PATH: src/main/java/com/uef/library/dto/PaymentResponseDTO.java
package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String qrCodeUrl;       // URL của ảnh QR Code (trong thực tế có thể là base64)
    private Long penaltyId;         // ID của khoản phạt để frontend có thể poll
    private String formattedAmount; // Số tiền đã được định dạng (VD: 50.000đ)
}