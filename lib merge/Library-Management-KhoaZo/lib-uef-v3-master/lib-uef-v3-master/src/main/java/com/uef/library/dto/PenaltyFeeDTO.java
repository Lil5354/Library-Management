package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyFeeDTO {
    private Long id;
    private Long borrowingId; // ID của lượt mượn liên quan
    private String readerId;
    private String readerName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn; // Thêm ISBN của sách
    private Integer overdueDays;
    private Double penaltyAmount;
    private String status; // Trạng thái: Chưa thanh toán, Đã thanh toán, Miễn giảm
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String waivedReason;
    private String collectedByUserName; // Tên thủ thư đã thu phí/miễn giảm
}