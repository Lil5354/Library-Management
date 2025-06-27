// Vị trí: com/uef/library/service/ReaderService.java
package com.uef.library.service;

import com.uef.library.config.UserSpecification;
import com.uef.library.dto.ReaderDTO;
import com.uef.library.model.User;
import com.uef.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ReaderService {

    @Autowired
    private UserRepository userRepository;

    private static final String READER_ROLE = "READER";

    // --- SỬA ĐỔI PHƯƠNG THỨC NÀY ---
    public Page<ReaderDTO> getReaders(Pageable pageable, String keyword, String status) {
        // Gọi UserSpecification mới với đầy đủ tham số
        Specification<User> spec = UserSpecification.filterReaders(keyword, READER_ROLE, status);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(this::convertToDto);
    }

    private ReaderDTO convertToDto(User user) {
        // Không cần thay đổi phương thức này
        return ReaderDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getUserDetail() != null ? user.getUserDetail().getFullName() : user.getUsername())
                .email(user.getUserDetail() != null ? user.getUserDetail().getEmail() : "N/A")
                .role(user.getRole())
                .status(user.isStatus())
                .membershipExpiryDate(user.getUserDetail() != null ? user.getUserDetail().getMembershipExpiryDate() : null)
                .build();
    }
}