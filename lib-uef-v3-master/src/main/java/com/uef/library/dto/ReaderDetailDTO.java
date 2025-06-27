package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO này dùng để truyền dữ liệu chi tiết của một độc giả,
 * phục vụ cho việc thêm mới, hiển thị form chỉnh sửa, và cập nhật.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderDetailDTO {

    private String userId;
    private String username;
    private String password; // Chỉ dùng khi tạo mới hoặc đổi mật khẩu
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private LocalDate dob;
    private boolean status;
    private LocalDate membershipExpiryDate;
    private String avatar;
    private String role; // Luôn là "READER" khi tạo

}