package com.uef.library.controller;

import com.uef.library.dto.ReaderDTO;
import com.uef.library.dto.ReaderDetailDTO;
import com.uef.library.model.LoanItemDto;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.service.BorrowServiceImpl;
import com.uef.library.service.ReaderManagementService;
import com.uef.library.service.ReaderService;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller thống nhất để quản lý các chức năng liên quan đến Độc giả.
 * Bao gồm cả các tác vụ của Admin và các tác vụ của người dùng (độc giả).
 */
@RestController
@RequestMapping("/api/readers") // Đường dẫn gốc cho tất cả các API liên quan đến độc giả
@RequiredArgsConstructor
public class ReaderController {

    // --- Khai báo tất cả các Service cần thiết ---
    private final ReaderService readerService;
    private final ReaderManagementService readerManagementService;
    private final UserService userService;
    private final BorrowServiceImpl borrowService;


    //================================================================================
    // CÁC API DÀNH CHO QUẢN LÝ (ADMIN)
    //================================================================================

    /**
     * [ADMIN] Lấy danh sách độc giả có phân trang, tìm kiếm và lọc.
     * Endpoint: GET /api/readers
     */
    @GetMapping
    public ResponseEntity<Page<ReaderDTO>> getAllReaders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId,asc") String[] sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        String sortField = sort[0];
        Sort.Direction sortDirection = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<ReaderDTO> readerPage = readerService.getReaders(pageable, keyword, status);
        return ResponseEntity.ok(readerPage);
    }

    /**
     * [ADMIN] Tạo một độc giả mới (đăng ký tài khoản cho độc giả).
     * Endpoint: POST /api/readers
     */
    @PostMapping
    public ResponseEntity<?> createReader(@RequestBody ReaderDetailDTO readerDto) {
        try {
            // Kiểm tra các trường bắt buộc
            if (readerDto.getUserId() == null || readerDto.getUserId().isEmpty() ||
                    readerDto.getUsername() == null || readerDto.getUsername().isEmpty() ||
                    readerDto.getPassword() == null || readerDto.getPassword().isEmpty() ||
                    readerDto.getFullName() == null || readerDto.getFullName().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng điền đầy đủ các trường bắt buộc (Mã, Tên đăng nhập, Mật khẩu, Họ tên).");
            }

            readerManagementService.createReader(readerDto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo độc giả: " + e.getMessage());
        }
    }

    /**
     * [ADMIN] Lấy thông tin chi tiết của một độc giả theo ID.
     * Endpoint: GET /api/readers/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ReaderDetailDTO> getReaderById(@PathVariable String userId) {
        Optional<ReaderDetailDTO> readerDetailDTO = readerManagementService.findReaderById(userId);
        return readerDetailDTO
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * [ADMIN] Cập nhật thông tin một độc giả.
     * Endpoint: PUT /api/readers/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateReader(@PathVariable String userId, @RequestBody ReaderDetailDTO readerDto) {
        try {
            readerManagementService.updateReader(userId, readerDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật độc giả: " + e.getMessage());
        }
    }

    /**
     * [ADMIN] Xóa một độc giả.
     * Endpoint: DELETE /api/readers/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteReader(@PathVariable String userId) {
        try {
            readerManagementService.deleteReader(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa độc giả. Có thể độc giả này có ràng buộc dữ liệu (như đang mượn sách).");
        }
    }


    //================================================================================
    // CÁC API DÀNH CHO ĐỘC GIẢ ĐÃ ĐĂNG NHẬP
    //================================================================================

    /**
     * [READER] Lấy thông tin cá nhân của độc giả đang đăng nhập.
     * Endpoint: GET /api/readers/profile
     */
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

    /**
     * [READER] Tự cập nhật thông tin cá nhân.
     * Endpoint: POST /api/readers/profile/update
     */
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> handleProfileUpdate(Authentication authentication,
                                                                   @RequestBody UserDetail userDetailFromForm,
                                                                   HttpSession session) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
        // 1. Ràng buộc ngày sinh không được ở tương lai
        if (userDetailFromForm.getDob() != null && userDetailFromForm.getDob().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ngày sinh không hợp lệ. Vui lòng không chọn một ngày trong tương lai."));
        }

        // 2. Ràng buộc số điện thoại không được trùng
        if (StringUtils.hasText(userDetailFromForm.getPhone())) {
            if (userService.phoneExistsForOtherUser(userDetailFromForm.getPhone(), currentUser.getUserId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại này đã được sử dụng bởi một tài khoản khác."));
            }
        }
        UserDetail detailsToUpdate = currentUser.getUserDetail();

        // Chỉ cập nhật email nếu email mới được gửi lên và khác với email cũ
        if (StringUtils.hasText(userDetailFromForm.getEmail()) && !userDetailFromForm.getEmail().equals(detailsToUpdate.getEmail())) {
            // Kiểm tra email mới có bị trùng không
            if (userService.emailExists(userDetailFromForm.getEmail(), username)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email này đã được sử dụng bởi một tài khoản khác."));
            }
            detailsToUpdate.setEmail(userDetailFromForm.getEmail());
        }

        // Chỉ cập nhật các trường khác nếu chúng được gửi lên (không phải null)
        if (userDetailFromForm.getFullName() != null) {
            detailsToUpdate.setFullName(userDetailFromForm.getFullName());
        }
        if (userDetailFromForm.getPhone() != null) {
            detailsToUpdate.setPhone(userDetailFromForm.getPhone());
        }
        if (userDetailFromForm.getAddress() != null) {
            detailsToUpdate.setAddress(userDetailFromForm.getAddress());
        }
        if (userDetailFromForm.getDob() != null) {
            detailsToUpdate.setDob(userDetailFromForm.getDob());
        }
        if (userDetailFromForm.getGender() != null) {
            detailsToUpdate.setGender(userDetailFromForm.getGender());
        }

        userService.saveUser(currentUser);

        // Nếu đây là lần cập nhật đầu tiên, xóa cờ trong session
        if (session.getAttribute("showFirstLoginPopup") != null) {
            session.removeAttribute("showFirstLoginPopup");
        }

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật thông tin thành công!",
                "newFullName", detailsToUpdate.getFullName()
        ));
    }
    /**
     * [READER] Tải lên ảnh đại diện.
     * Endpoint: POST /api/readers/profile/avatar/upload
     */
    @PostMapping("/profile/avatar/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(Authentication authentication, @RequestParam("avatarFile") MultipartFile file) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);

        if (currentUser == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu không hợp lệ."));
        }

        Path uploadPath = Paths.get("uploads/avatars");
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Map.of("message", "Không thể tạo thư mục lưu trữ."));
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = currentUser.getUserId() + "_" + System.currentTimeMillis() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatars/" + uniqueFilename;
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

    /**
     * [READER] Xem danh sách sách đang mượn.
     * Endpoint: GET /api/readers/profile/borrowed-books
     */
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