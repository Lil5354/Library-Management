package com.uef.library.service;

import com.uef.library.config.UserSpecification;
import com.uef.library.dto.ReaderDTO;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail; // <<< THÊM DÒNG NÀY
import com.uef.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate; // <<< THÊM DÒNG NÀY

@Service
public class ReaderService {

    @Autowired
    private UserRepository userRepository;

    private static final String READER_ROLE = "READER";

    public Page<ReaderDTO> getReaders(Pageable pageable, String keyword, String status) {
        // Gọi UserSpecification mới với đầy đủ tham số
        Specification<User> spec = UserSpecification.filterReaders(keyword, READER_ROLE, status);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(this::convertToDto);
    }

    private ReaderDTO convertToDto(User user) {
        UserDetail detail = user.getUserDetail();
        LocalDate expiryDate = null;

        if (detail != null) {
            // Lấy ngày hết hạn từ database
            expiryDate = detail.getMembershipExpiryDate();
        }

        // Nếu ngày hết hạn vẫn là null (đối với các tài khoản cũ)
        // và tài khoản có ngày tạo, thì sẽ tính ngày hết hạn mặc định
        if (expiryDate == null && user.getCreatedAt() != null) {
            // Ngày hết hạn = Ngày tạo tài khoản + 1 năm
            expiryDate = user.getCreatedAt().toLocalDate().plusYears(1);
        }

        return ReaderDTO.builder()
                .userId(user.getUserId())
                .fullName(detail != null ? detail.getFullName() : user.getUsername())
                .email(detail != null ? detail.getEmail() : "N/A")
                .role(user.getRole())
                .status(user.isStatus())
                .membershipExpiryDate(expiryDate) // Sử dụng ngày hết hạn đã được tính toán
                .build();
    }
}