package com.uef.library.controller;

import com.uef.library.model.User;
import com.uef.library.service.PasswordResetServiceImpl;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetServiceImpl passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        if (user.getUserDetail().getEmail() == null || user.getUserDetail().getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "NO_EMAIL"));
        }
        passwordResetService.createAndSendPasswordResetToken(user);
        return ResponseEntity.ok(Map.of("message", "Mã xác thực đã được gửi đến email của bạn."));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
        String result = passwordResetService.validatePasswordResetToken(payload.get("token"));
        if (result != null) {
            return ResponseEntity.badRequest().body(Map.of("error", result));
        }
        return ResponseEntity.ok(Map.of("message", "valid"));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        // 1. Kiểm tra token có hợp lệ không
        String validationResult = passwordResetService.validatePasswordResetToken(token);
        if (validationResult != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn."));
        }

        // 2. Dùng token để lấy ra người dùng
        User user = passwordResetService.getUserByPasswordResetToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token không liên kết với người dùng nào."));
        }

        // 3. Gọi hàm đổi mật khẩu với đúng tham số
        passwordResetService.changeUserPassword(user, newPassword);

        // 4. Xóa token sau khi đã sử dụng thành công
        passwordResetService.deleteToken(token);
        // === THÊM LOGIC ĐĂNG XUẤT NGƯỜI DÙNG ===
        new SecurityContextLogoutHandler().logout(request, null, null);

        return ResponseEntity.ok(Map.of(
                "message", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                "logout", true
        ));    }
}