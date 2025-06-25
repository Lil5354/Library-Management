package com.uef.library.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;import com.uef.library.model.LoanItemDto;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.uef.library.service.BorrowServiceImpl;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
    // === API MỚI: XỬ LÝ UPLOAD AVATAR ===
    @PostMapping("/profile/avatar/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(Authentication authentication, @RequestParam("avatarFile") MultipartFile file) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);

        if (currentUser == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu không hợp lệ."));
        }

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get("uploads/avatars");
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Map.of("message", "Không thể tạo thư mục lưu trữ."));
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Tạo tên file duy nhất để tránh bị ghi đè
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = currentUser.getUserId() + "_" + System.currentTimeMillis() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật đường dẫn avatar vào database
            String avatarUrl = "/uploads/avatars/" + uniqueFilename; // Đường dẫn mà trình duyệt có thể truy cập
            UserDetail userDetail = currentUser.getUserDetail();
            userDetail.setAvatar(avatarUrl);
            userService.saveUser(currentUser);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật ảnh đại diện thành công!",
                    "avatarUrl", avatarUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi khi lưu file ảnh."));
        }
    }
    private final BorrowServiceImpl borrowService;
    // API cho chức năng "Sách đang mượn" (Popup 1)
    @GetMapping("/borrowed-books")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoanItemDto>> getBorrowedBooks(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // Service sẽ trả về danh sách các sách đang mượn
        List<LoanItemDto> activeLoans = borrowService.getActiveLoansForUser(user);
        return ResponseEntity.ok(activeLoans);
    }

    // API cho chức năng "Lịch sử mượn sách" (Popup 2)
    @GetMapping("/borrowing-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LoanItemDto>> getBorrowingHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        Pageable pageable = PageRequest.of(page, size);
        // Service sẽ trả về một trang LỊCH SỬ đầy đủ
        Page<LoanItemDto> historyPage = borrowService.getLoanHistoryForUser(user, pageable);
        return ResponseEntity.ok(historyPage);
    }

}