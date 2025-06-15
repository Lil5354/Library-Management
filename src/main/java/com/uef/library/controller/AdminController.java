package com.uef.library.controller;

import com.uef.library.model.MarqueeMessage;
import com.uef.library.model.Notification;
import com.uef.library.model.User;
// import com.uef.library.model.UserDetail; // Đã được xử lý qua User
import com.uef.library.model.UserDetail;
import com.uef.library.service.MarqueeNotificationService;
import com.uef.library.service.NotificationService;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private MarqueeNotificationService marqueeNotificationService;
    @Autowired
    private NotificationService notificationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session,
                                 Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(name = "keyword", required = false) String keyword,
                                 @RequestParam(name = "role", required = false) String role,
                                 @RequestParam(name = "status", required = false) String statusStr) {
        // ... (code hiện tại của bạn) ...
        String currentUsername = (String) session.getAttribute("username");
        User currentUser = userService.findByUsername(currentUsername).orElse(null);

        String fullName = "bạn";
        if (currentUser != null && currentUser.getUserDetail() != null) {
            String userDetailFullName = currentUser.getUserDetail().getFullName();
            if (userDetailFullName != null && !userDetailFullName.trim().isEmpty() && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                fullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", fullName);
        // Các model attributes khác cho dashboard
        model.addAttribute("adminCount", userService.countAdmins());
        model.addAttribute("staffCount", userService.countStaffs());
        model.addAttribute("readerCount", userService.countReaders());
        model.addAttribute("lockCount", userService.countLockedAccounts());

        Pageable pageable = PageRequest.of(page, size);
        Boolean statusBoolean = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                statusBoolean = Boolean.parseBoolean(statusStr);
            } catch (Exception e) { /* Bỏ qua */ }
        }
        Page<User> userPage = userService.findPaginatedUsersWithFilter(keyword, role, statusBoolean, pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", statusStr);
        return "admin/index";
    }

    // --- ENDPOINT CHO TRANG QUẢN LÝ SÁCH TOÀN DIỆN ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/comprehensive-book-management")
    public String comprehensiveBookManagementPage(Model model, HttpSession session) {
        // Lấy thông tin admin hiện tại để hiển thị lời chào (nếu cần)
        String currentUsername = (String) session.getAttribute("username");
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        String fullName = "Admin";
        if (currentUser != null && currentUser.getUserDetail() != null) {
            String userDetailFullName = currentUser.getUserDetail().getFullName();
            if (userDetailFullName != null && !userDetailFullName.trim().isEmpty() && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                fullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", fullName);
        // Bạn có thể thêm các model attributes khác nếu trang này cần dữ liệu gì đó
        return "admin/comprehensive_book_management"; // Trỏ đến file HTML mới
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system-management")
    public String systemManagementPage(Model model, HttpSession session) {
        // Lấy thông tin admin hiện tại để hiển thị lời chào (nếu cần)
        String currentUsername = (String) session.getAttribute("username");
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        String fullName = "Admin";
        if (currentUser != null && currentUser.getUserDetail() != null) {
            String userDetailFullName = currentUser.getUserDetail().getFullName();
            if (userDetailFullName != null && !userDetailFullName.trim().isEmpty() && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                fullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", fullName);
        // Bạn có thể thêm các model attributes khác nếu trang này cần dữ liệu gì đó ban đầu
        return "admin/system_management"; // Trỏ đến file HTML mới
    }

    // --- CÁC API ENDPOINT KHÁC GIỮ NGUYÊN ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/filter")
    @ResponseBody
    public ResponseEntity<Page<User>> getFilteredUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "status", required = false) String statusStr) {
        Pageable pageable = PageRequest.of(page, size);
        Boolean statusBoolean = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                statusBoolean = Boolean.parseBoolean(statusStr);
            } catch (Exception e) { /* Bỏ qua */ }
        }
        Page<User> userPage = userService.findPaginatedUsersWithFilter(keyword, role, statusBoolean, pageable);
        return ResponseEntity.ok(userPage);
    }

    // ... (các endpoint @PutMapping, @DeleteMapping, @GetMapping("/api/users/{id}/details") giữ nguyên) ...
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/users/{id}/update-role")
    public ResponseEntity<String> updateUserRole(@PathVariable("id") String userId,
                                                 @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || !(newRole.equalsIgnoreCase("STAFF") || newRole.equalsIgnoreCase("READER"))) {
            return ResponseEntity.badRequest().body("Vai trò không hợp lệ. Chỉ chấp nhận STAFF hoặc READER.");
        }
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với ID: " + userId);
        }
        User userToUpdate = userOpt.get();
        if ("ADMIN".equalsIgnoreCase(userToUpdate.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể thay đổi vai trò của tài khoản ADMIN.");
        }
        boolean success = userService.updateUserRole(userId, newRole.toUpperCase());
        if (success) {
            return ResponseEntity.ok("Cập nhật vai trò thành công cho người dùng ID: " + userId + " thành " + newRole.toUpperCase());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi không xác định khi cập nhật vai trò.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/users/{id}/delete")
    public ResponseEntity<String> deleteUserAccount(@PathVariable("id") String userIdToDelete,
                                                    HttpSession session) {
        String currentAdminUsername = (String) session.getAttribute("username");
        if (currentAdminUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không xác định được người thực hiện thao tác.");
        }
        Optional<User> adminUserOpt = userService.findByUsername(currentAdminUsername);
        if (adminUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng quản trị không hợp lệ.");
        }
        if (adminUserOpt.get().getUserId().equals(userIdToDelete)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không thể tự xóa tài khoản của chính mình.");
        }
        Optional<User> userToBeDeletedOpt = userService.findById(userIdToDelete);
        if(userToBeDeletedOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng để xóa với ID: " + userIdToDelete);
        }
        boolean success = userService.deleteUser(userIdToDelete);
        if (success) {
            return ResponseEntity.ok("Đã xóa thành công người dùng ID: " + userIdToDelete);
        } else {
            User userAttempted = userToBeDeletedOpt.get();
            if ("ADMIN".equalsIgnoreCase(userAttempted.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa tài khoản có vai trò ADMIN.");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa người dùng ID: " + userIdToDelete + ". Có thể do quyền hạn hoặc lỗi hệ thống.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{id}/details")
    public ResponseEntity<?> getUserDetailsForEdit(@PathVariable("id") String userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với ID: " + userId);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/users/{id}/update-details")
    public ResponseEntity<String> updateUserDetails(@PathVariable("id") String userId,
                                                    @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với ID: " + userId);
        }
        User user = userOpt.get();
        UserDetail userDetail = user.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setUser(user);
            user.setUserDetail(userDetail);
        }

        if (updates.containsKey("fullName")) userDetail.setFullName((String) updates.get("fullName"));
        if (updates.containsKey("email")) userDetail.setEmail((String) updates.get("email"));
        if (updates.containsKey("phone")) userDetail.setPhone((String) updates.get("phone"));
        if (updates.containsKey("address")) userDetail.setAddress((String) updates.get("address"));
        if (updates.containsKey("gender")) userDetail.setGender((String) updates.get("gender"));
        if (updates.containsKey("avatar")) userDetail.setAvatar((String) updates.get("avatar"));
        if (updates.containsKey("dob")) {
            String dobStr = (String) updates.get("dob");
            if (dobStr != null && !dobStr.isEmpty()) {
                try {
                    userDetail.setDob(java.time.LocalDate.parse(dobStr));
                } catch (java.time.format.DateTimeParseException e) {
                    return ResponseEntity.badRequest().body("Định dạng ngày sinh không hợp lệ. Vui lòng dùng yyyy-MM-dd.");
                }
            } else {
                userDetail.setDob(null);
            }
        }
        if (updates.containsKey("status") && updates.get("status") != null) {
            boolean newStatus = Boolean.parseBoolean(String.valueOf(updates.get("status")));
            if("ADMIN".equalsIgnoreCase(user.getRole()) && !newStatus){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa tài khoản ADMIN.");
            }
            user.setStatus(newStatus);
        }
        boolean success = userService.saveUser(user);
        if (success) {
            return ResponseEntity.ok("Cập nhật thông tin người dùng thành công.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin người dùng.");
        }
    }

    // --- QUẢN LÝ THÔNG BÁO MARQUEE ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications/marquee")
    public String manageMarqueeNotificationPage(Model model, HttpSession session) {
        String currentAdminUsername = (String) session.getAttribute("username");
        User currentAdminUser = userService.findByUsername(currentAdminUsername).orElse(null);

        String adminFullName = "Admin"; // Giá trị mặc định
        if (currentAdminUser != null && currentAdminUser.getUserDetail() != null) {
            String userDetailFullName = currentAdminUser.getUserDetail().getFullName();
            if (userDetailFullName != null && !userDetailFullName.trim().isEmpty() && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                adminFullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", adminFullName); // Cho layout admin (nếu layout có dùng)

        // Lấy thông tin thông báo marquee hiện tại
        Optional<MarqueeMessage> currentMessageOpt = marqueeNotificationService.getCurrentMarqueeMessage();

        // Truyền thông tin marquee vào model để hiển thị trên form
        if (currentMessageOpt.isPresent()) {
            model.addAttribute("currentMarqueeMessageContent", currentMessageOpt.get().getContent());
            model.addAttribute("isMarqueeEnabled", currentMessageOpt.get().isEnabled());
        } else {
            model.addAttribute("currentMarqueeMessageContent", "");
            model.addAttribute("isMarqueeEnabled", false);
        }

        return "admin/manage_marquee_notification";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications/marquee/update")
    public String updateMarqueeNotification(@RequestParam("marqueeContent") String content,
                                            @RequestParam(name = "marqueeEnabled", defaultValue = "false") boolean enabled, // defaultValue nếu checkbox không được tick
                                            RedirectAttributes redirectAttributes) {
        try {
            marqueeNotificationService.updateMarqueeMessage(content, enabled);
            redirectAttributes.addFlashAttribute("successMessageMarquee", "Thông báo marquee đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessageMarquee", "Lỗi khi cập nhật thông báo marquee: " + e.getMessage());
            // Log lỗi e.printStackTrace();
        }
        return "redirect:/admin/notifications/marquee";
    }

    // --- QUẢN LÝ GỬI THÔNG BÁO NGƯỜI DÙNG (CHUÔNG) ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications/user/send")
    public String showSendUserNotificationPage(Model model, HttpSession session) { // Thêm HttpSession

        // === BẮT ĐẦU THÊM CODE MỚI ===
        // Lấy thông tin admin hiện tại để hiển thị lời chào
        String currentUsername = (String) session.getAttribute("username");
        if (currentUsername != null) {
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            String fullName = "Admin"; // Giá trị mặc định

            if (currentUser != null && currentUser.getUserDetail() != null) {
                String userDetailFullName = currentUser.getUserDetail().getFullName();
                if (userDetailFullName != null && !userDetailFullName.trim().isEmpty() && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                    fullName = userDetailFullName;
                }
            }
            model.addAttribute("fullName", fullName);
        } else {
            // Phòng trường hợp không lấy được username từ session
            model.addAttribute("fullName", "Admin");
        }
        // === KẾT THÚC THÊM CODE MỚI ===

        return "admin/send_user_notification";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications/user/send")
    // Thay đổi 1: Thêm @RequestParam String title
    public String handleSendUserNotification(@RequestParam String targetType,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false) String username,
                                             @RequestParam String title, // Thêm dòng này
                                             @RequestParam String message,
                                             @RequestParam(required = false) String link,
                                             RedirectAttributes redirectAttributes) {
        try {
            // ... (phần switch case định nghĩa 'type' và 'targetIdentifier' giữ nguyên)
            Notification.NotificationTargetType type;
            String targetIdentifier = null;

            switch (targetType) {
                case "all":
                    type = Notification.NotificationTargetType.ALL_USERS;
                    break;
                case "role":
                    type = Notification.NotificationTargetType.BY_ROLE;
                    targetIdentifier = role;
                    break;
                case "single":
                    type = Notification.NotificationTargetType.SINGLE_USER;
                    targetIdentifier = username;
                    break;
                default:
                    throw new IllegalArgumentException("Mục tiêu gửi không hợp lệ.");
            }

            // Thay đổi 2: Truyền 'title' vào service
            notificationService.sendNotification(type, targetIdentifier, title, message, link);
            redirectAttributes.addFlashAttribute("successMessageUserNotify", "Đã gửi thông báo thành công!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessageUserNotify", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessageUserNotify", "Đã xảy ra lỗi không mong muốn khi gửi thông báo.");
        }

        return "redirect:/admin/notifications/user/send";
    }
}