package com.uef.library.service;

import com.uef.library.model.PasswordResetToken;
import com.uef.library.model.User;
import com.uef.library.repository.PasswordResetTokenRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmailTemplateService emailTemplateService;
    @Override
    @Transactional
    public void createAndSendPasswordResetToken(User user) {

        // === BẮT ĐẦU SỬA LỖI: LOGIC "UPDATE OR INSERT" ===

        // 1. Thử tìm token đã tồn tại của người dùng
        PasswordResetToken myToken = tokenRepository.findByUser(user);

        // 2. Nếu chưa có token, tạo một đối tượng mới và liên kết với người dùng
        if (myToken == null) {
            myToken = new PasswordResetToken();
            myToken.setUser(user);
        }

        // 3. Tạo mã OTP mới và cập nhật thời gian hết hạn (cho cả token cũ và mới)
        String tokenCode = String.format("%06d", new java.util.Random().nextInt(999999));
        myToken.setToken(tokenCode);
        myToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        // 4. Lưu lại token. Spring Data JPA sẽ tự động UPDATE nếu token đã tồn tại,
        // hoặc INSERT nếu là token mới.
        tokenRepository.save(myToken);

        // ===============================================

        // 5. Gửi email với mã OTP mới
        String emailBody = emailTemplateService.generateHtmlFromTemplate(
                "password-reset-template.html",
                Map.of("name", user.getUserDetail().getFullName(), "otp", tokenCode)
        );
        emailService.sendHtmlEmail(user.getUserDetail().getEmail(), "Yêu cầu Đặt lại Mật khẩu - Thư viện Số UEF", emailBody);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = tokenRepository.findByToken(token);

        if (passToken == null) {
            return "invalidToken"; // Token không tồn tại
        }

        if (passToken.isExpired()) {
            tokenRepository.delete(passToken); // Xóa token hết hạn khỏi DB
            return "expired"; // Token đã hết hạn
        }

        return null; // Token hợp lệ
    }

    @Override
    public User getUserByPasswordResetToken(String token) {
        PasswordResetToken passToken = tokenRepository.findByToken(token);
        if (passToken != null) {
            return passToken.getUser();
        }
        return null;
    }

    @Override
    @Transactional
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        tokenRepository.delete(tokenRepository.findByToken(token));
    }
}