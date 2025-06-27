package com.uef.library.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReaderDTO {
    private String userId; // Mã thẻ
    private String fullName; // Họ tên
    private String email;
    private String role; // Phân loại (Sinh viên, Giáo viên...)
    private boolean status; // Trạng thái tài khoản (true: Hoạt động, false: Khóa)
    private LocalDate membershipExpiryDate; // Ngày hết hạn thẻ
}