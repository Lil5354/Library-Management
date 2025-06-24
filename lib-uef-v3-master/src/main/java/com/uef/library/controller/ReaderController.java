package com.uef.library.controller;

import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reader/api")
@RequiredArgsConstructor
public class ReaderController {

    private final UserService userService;

    // API ĐỂ LẤY THÔNG TIN PROFILE CỦA USER HIỆN TẠI
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(currentUser);
    }

    // API ĐỂ XỬ LÝ CẬP NHẬT PROFILE
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> handleFirstUpdate(Authentication authentication,
                                                                 @RequestBody UserDetail userDetailFromForm,
                                                                 HttpSession session) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        UserDetail detailsToUpdate = currentUser.getUserDetail();
        detailsToUpdate.setFullName(userDetailFromForm.getFullName());
        detailsToUpdate.setPhone(userDetailFromForm.getPhone());
        detailsToUpdate.setEmail(userDetailFromForm.getEmail());
        detailsToUpdate.setAddress(userDetailFromForm.getAddress());
        detailsToUpdate.setDob(userDetailFromForm.getDob());
        detailsToUpdate.setGender(userDetailFromForm.getGender());

        userService.saveUser(currentUser);

        // Xóa cờ hiệu trong session để popup không hiện lại nữa
        session.removeAttribute("showFirstLoginPopup");

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật thông tin thành công! Chào mừng đến với Thư viện Số.",
                "newFullName", detailsToUpdate.getFullName()
        ));
    }
}