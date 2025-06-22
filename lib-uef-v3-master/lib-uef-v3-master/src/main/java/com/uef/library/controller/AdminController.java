package com.uef.library.controller;

import com.uef.library.model.MarqueeMessage;
import com.uef.library.model.Notification;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.service.*;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private MarqueeNotificationService marqueeNotificationService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TrainAiService trainAiService;
    @Autowired
    private ChatPromptService chatPromptService;
    @Autowired
    private EmailService emailService;

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
        User currentAdminUser = userService.findByUsername(currentAdminUsername).orElse(null); // cite: AdminController.java

        String adminFullName = "Admin";
        if (currentAdminUser != null && currentAdminUser.getUserDetail() != null) { // cite: AdminController.java
            String userDetailFullName = currentAdminUser.getUserDetail().getFullName(); // cite: AdminController.java
            if (StringUtils.hasText(userDetailFullName) && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                adminFullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", adminFullName);

        Optional<MarqueeMessage> currentMessageOpt = marqueeNotificationService.getCurrentMarqueeMessage(); // cite: MarqueeNotificationService.java

        if (currentMessageOpt.isPresent()) {
            model.addAttribute("currentMarqueeMessageContent", currentMessageOpt.get().getContent()); // cite: MarqueeMessage.java
            model.addAttribute("isMarqueeEnabled", currentMessageOpt.get().isEnabled()); // cite: MarqueeMessage.java
        } else {
            model.addAttribute("currentMarqueeMessageContent", "");
            model.addAttribute("isMarqueeEnabled", false);
        }

        // === THÊM DANH SÁCH CÁC MẪU MARQUEE VÀO MODEL ===
        model.addAttribute("marqueeTemplates", marqueeNotificationService.getMarqueeTemplates()); // Thêm dòng này
        // ===============================================

        return "admin/manage_marquee_notification"; // cite: manage_marquee_notification.html
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications/marquee/update")
    public String updateMarqueeNotification(@RequestParam("marqueeContent") String content,
                                            @RequestParam(name = "marqueeEnabled", defaultValue = "false") boolean enabled,
                                            RedirectAttributes redirectAttributes) {
        try {
            marqueeNotificationService.updateMarqueeMessage(content, enabled); // cite: MarqueeNotificationService.java
            redirectAttributes.addFlashAttribute("successMessageMarquee", "Thông báo marquee đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessageMarquee", "Lỗi khi cập nhật thông báo marquee: " + e.getMessage());
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
            User currentUser = userService.findByUsername(currentUsername).orElse(null); // cite: AdminController.java
            String fullName = "Admin";

            if (currentUser != null && currentUser.getUserDetail() != null) { // cite: AdminController.java
                String userDetailFullName = currentUser.getUserDetail().getFullName(); // cite: AdminController.java
                if (StringUtils.hasText(userDetailFullName) && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                    fullName = userDetailFullName;
                }
            }
            model.addAttribute("fullName", fullName);
        } else {
            model.addAttribute("fullName", "Admin");
        }
        // === KẾT THÚC THÊM CODE MỚI ===

        // === THÊM DANH SÁCH CÁC MẪU THÔNG BÁO NGƯỜI DÙNG VÀO MODEL ===
        model.addAttribute("userNotificationTemplates", notificationService.getUserNotificationTemplates());
        // ===============================================

        return "admin/send_user_notification"; // cite: send_user_notification.html
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications/user/send")
    public String handleSendUserNotification(@RequestParam String targetType,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false) String username,
                                             @RequestParam String title,
                                             @RequestParam String message,
                                             @RequestParam(required = false) String link,
                                             RedirectAttributes redirectAttributes) {
        try {
            Notification.NotificationTargetType type; // cite: Notification.java
            String targetIdentifier = null;

            switch (targetType) {
                case "all":
                    type = Notification.NotificationTargetType.ALL_USERS; // cite: Notification.java
                    break;
                case "role":
                    type = Notification.NotificationTargetType.BY_ROLE; // cite: Notification.java
                    targetIdentifier = role;
                    break;
                case "single":
                    type = Notification.NotificationTargetType.SINGLE_USER; // cite: Notification.java
                    targetIdentifier = username;
                    break;
                default:
                    throw new IllegalArgumentException("Mục tiêu gửi không hợp lệ.");
            }

            notificationService.sendNotification(type, targetIdentifier, title, message, link); // cite: NotificationService.java
            redirectAttributes.addFlashAttribute("successMessageUserNotify", "Đã gửi thông báo thành công!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessageUserNotify", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessageUserNotify", "Đã xảy ra lỗi không mong muốn khi gửi thông báo.");
        }

        return "redirect:/admin/notifications/user/send";
    }

    // --- ENDPOINTS CHO TRANG TRAIN AI ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/train-ai")
    public String showTrainAiPage(Model model, HttpSession session) {
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
        model.addAttribute("personas", trainAiService.getAllPersonas());
        model.addAttribute("activePersona", trainAiService.getActivePersonaName());
        model.addAttribute("customPersonaTemplate", trainAiService.getCustomPersonaTemplate());
        return "admin/train-ai";

    }

    @PostMapping("/train-ai/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public String handleActivatePersona(@RequestParam("personaName") String personaName, RedirectAttributes redirectAttributes) {
        try {
            trainAiService.setActivePersona(personaName);
            chatPromptService.reloadKnowledge();
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt persona thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/train-ai";
    }

    @PostMapping("/train-ai/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String handleCreatePersona(@RequestParam("displayName") String displayName,
                                      @RequestParam(name = "description", required = false) String description, // Thêm tham số này
                                      @RequestParam("knowledge") String knowledge,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Truyền cả description vào service
            trainAiService.createNewPersona(displayName, description, knowledge);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo persona '" + displayName + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/train-ai";
    }

    @PostMapping("/train-ai/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String handleDeletePersona(@RequestParam("fileName") String fileName, RedirectAttributes redirectAttributes) {
        try {
            trainAiService.deleteCustomPersona(fileName);
            chatPromptService.reloadKnowledge();
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa persona thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/train-ai";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications/email/send")
    public String showSendEmailNotificationPage(Model model, HttpSession session) {
        String currentUsername = (String) session.getAttribute("username");
        User currentUser = userService.findByUsername(currentUsername).orElse(null);

        String fullName = "Admin";
        if (currentUser != null && currentUser.getUserDetail() != null) {
            String userDetailFullName = currentUser.getUserDetail().getFullName();
            if (StringUtils.hasText(userDetailFullName) && !"Chưa cập nhật".equalsIgnoreCase(userDetailFullName.trim())) {
                fullName = userDetailFullName;
            }
        }
        model.addAttribute("fullName", fullName);

        // Chuẩn bị dữ liệu cho các form email thiết kế sẵn (nếu cần)
        // Ví dụ:
        model.addAttribute("staffEmailTemplates", getStaffEmailTemplates());
        model.addAttribute("readerEmailTemplates", getReaderEmailTemplates());

        return "admin/send_email_notification";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications/email/send")
    public String handleSendEmailNotification(@RequestParam String targetType,
                                              @RequestParam(required = false) String role,
                                              @RequestParam(required = false) String username,
                                              @RequestParam String subject,
                                              @RequestParam String content,
                                              @RequestParam(defaultValue = "plain") String format,
                                              @RequestParam(name = "attachment", required = false) MultipartFile attachmentFile,
                                              RedirectAttributes redirectAttributes) {
        String attachmentPath = null;
        String attachmentFileName = null;

        // Xử lý file đính kèm nếu có
        if (attachmentFile != null && !attachmentFile.isEmpty() && attachmentFile.getSize() > 0) {
            try {
                Path uploadDir = Paths.get("uploads", "attachments"); // Sử dụng Paths.get("uploads", "attachments")
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFileName = StringUtils.cleanPath(attachmentFile.getOriginalFilename());
                // Ngăn chặn ghi đè file trùng tên
                String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                Path filePath = uploadDir.resolve(uniqueFileName);
                Files.copy(attachmentFile.getInputStream(), filePath);

                attachmentPath = filePath.toAbsolutePath().toString(); // Lấy đường dẫn tuyệt đối
                attachmentFileName = originalFileName; // Tên gốc của file

                System.out.println("Saved attachment to: " + attachmentPath);

            } catch (IOException e) {
                System.err.println("Error saving attachment file: " + e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessageEmail", "Lỗi khi lưu tệp đính kèm: " + e.getMessage());
                return "redirect:/admin/notifications/email/send";
            }
        }

        try {
            List<User> recipients;
            switch (targetType) {
                case "all":
                    recipients = userService.findAllUsers().stream()
                            .filter(user -> !"ADMIN".equals(user.getRole()))
                            .toList();
                    break;
                case "role":
                    if (!StringUtils.hasText(role)) {
                        throw new IllegalArgumentException("Phải chọn vai trò khi gửi email theo nhóm.");
                    }
                    recipients = userService.findByRole(role.toUpperCase());
                    break;
                case "single":
                    if (!StringUtils.hasText(username)) {
                        throw new IllegalArgumentException("Phải nhập username khi gửi email cho người dùng đơn lẻ.");
                    }
                    User user = userService.findByUsername(username)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));
                    recipients = Collections.singletonList(user);
                    break;
                default:
                    throw new IllegalArgumentException("Loại mục tiêu không hợp lệ.");
            }

            int emailsSent = 0;
            for (User recipient : recipients) {
                if (recipient.getUserDetail() != null && StringUtils.hasText(recipient.getUserDetail().getEmail())
                        && !recipient.getUserDetail().getEmail().equalsIgnoreCase("Chưa cập nhật")) {
                    emailService.sendEmail(recipient.getUserDetail().getEmail(), subject, content, format, attachmentPath, attachmentFileName);
                    emailsSent++;
                } else {
                    System.err.println("Skipping email for user " + recipient.getUsername() + ": No valid email address.");
                }
            }

            // Xóa file đính kèm tạm thời sau khi gửi xong
            if (attachmentPath != null) {
                try {
                    Files.deleteIfExists(Paths.get(attachmentPath));
                    System.out.println("Deleted temporary attachment file: " + attachmentPath);
                } catch (IOException e) {
                    System.err.println("Error deleting temporary attachment file: " + e.getMessage());
                }
            }

            if (emailsSent > 0) {
                redirectAttributes.addFlashAttribute("successMessageEmail", "Đã gửi " + emailsSent + " email thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessageEmail", "Không có email nào được gửi. Vui lòng kiểm tra thông tin người nhận và email.");
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessageEmail", "Lỗi: " + e.getMessage());
            // Xóa file đính kèm nếu có lỗi trước khi gửi
            if (attachmentPath != null) {
                try { Files.deleteIfExists(Paths.get(attachmentPath)); } catch (IOException ex) { /* ignore */ }
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessageEmail", "Đã xảy ra lỗi không mong muốn khi gửi email: " + e.getMessage());
            // Xóa file đính kèm nếu có lỗi trước khi gửi
            if (attachmentPath != null) {
                try { Files.deleteIfExists(Paths.get(attachmentPath)); } catch (IOException ex) { /* ignore */ }
            }
        }
        return "redirect:/admin/notifications/email/send";
    }

    // --- Phương thức hỗ trợ tạo template email mặc định ---
    private List<Map<String, String>> getStaffEmailTemplates() {
        return List.of(
                Map.of("id", "staff_new_policy", "name", "Thông báo Quy định mới", "subject", "Thông báo: Cập nhật Quy định Thư viện mới", "content", """
                <p>Kính gửi quý Nhân viên,</p>
                <p>Chúng tôi xin thông báo về việc cập nhật một số quy định mới của thư viện, có hiệu lực từ ngày [NGÀY HIỆU LỰC].</p>
                <p>Vui lòng xem chi tiết các quy định tại [LIÊN KẾT ĐẾN VĂN BẢN QUY ĐỊNH].</p>
                <p>Trân trọng cảm ơn,</p>
                <p>Ban Quản lý Thư viện Số UEF</p>
                """, "format", "html"),
                Map.of("id", "staff_training_schedule", "name", "Lịch đào tạo mới", "subject", "Lịch đào tạo nghiệp vụ Thư viện Quý tới", "content", """
                <p>Kính gửi quý Nhân viên,</p>
                <p>Chúng tôi xin gửi đến quý vị lịch đào tạo nghiệp vụ thư viện cho quý sắp tới.</p>
                <p>Vui lòng sắp xếp thời gian tham gia đầy đủ để nâng cao kỹ năng và nghiệp vụ.</p>
                <p>Chi tiết lịch đào tạo và đăng ký tại [LIÊN KẾT ĐẾN LỊCH ĐÀO TẠO].</p>
                <p>Trân trọng,</p>
                <p>Ban Tổ chức</p>
                """, "format", "html"),
                Map.of("id", "staff_system_maintenance", "name", "Thông báo bảo trì hệ thống", "subject", "Thông báo bảo trì hệ thống Thư viện Số", "content", """
                <p>Kính gửi quý Nhân viên,</p>
                <p>Hệ thống Thư viện Số sẽ tiến hành bảo trì định kỳ vào lúc [THỜI GIAN] ngày [NGÀY]. Trong thời gian này, hệ thống có thể tạm thời không truy cập được.</p>
                <p>Chúng tôi xin lỗi vì sự bất tiện này và mong quý vị thông cảm.</p>
                <p>Trân trọng,</p>
                <p>Đội ngũ Kỹ thuật Thư viện</p>
                """, "format", "html")
        );
    }

    private List<Map<String, String>> getReaderEmailTemplates() {
        return List.of(
                Map.of("id", "reader_new_book_arrival", "name", "Thông báo Sách mới về", "subject", "Sách mới đã có tại Thư viện Số UEF!", "content", """
                <p>Chào bạn Độc giả thân mến,</p>
                <p>Chúng tôi vui mừng thông báo rằng nhiều đầu sách mới hấp dẫn đã được bổ sung vào kho tài liệu của Thư viện Số UEF.</p>
                <p>Hãy khám phá ngay tại: <a href="[LIÊN KẾT ĐẾN DANH MỤC SÁCH MỚI]">Danh mục sách mới</a></p>
                <p>Chúc bạn có những trải nghiệm đọc sách tuyệt vời!</p>
                <p>Trân trọng,</p>
                <p>Thư viện Số UEF</p>
                """, "format", "html"),
                Map.of("id", "reader_upcoming_event", "name", "Thông báo Sự kiện", "subject", "Mời tham gia Sự kiện đặc biệt tại Thư viện Số UEF!", "content", """
                <p>Chào bạn Độc giả,</p>
                <p>Thư viện Số UEF sẽ tổ chức sự kiện [TÊN SỰ KIỆN] vào lúc [THỜI GIAN] ngày [NGÀY] tại [ĐỊA ĐIỂM/LIÊN KẾT ONLINE].</p>
                <p>Đây là cơ hội tuyệt vời để [LỢI ÍCH CỦA SỰ KIỆN].</p>
                <p>Đăng ký tham gia ngay tại: <a href="[LIÊN KẾT ĐẾN ĐĂNG KÝ SỰ KIỆN]">Link đăng ký</a></p>
                <p>Hẹn gặp lại bạn!</p>
                <p>Thư viện Số UEF</p>
                """, "format", "html"),
                Map.of("id", "reader_overdue_reminder", "name", "Nhắc nhở Sách quá hạn", "subject", "Nhắc nhở: Sách của bạn sắp hoặc đã quá hạn!", "content", """
                <p>Chào bạn Độc giả,</p>
                <p>Chúng tôi xin nhắc nhở rằng sách "[TÊN SÁCH]" của bạn có hạn trả vào ngày [NGÀY HẠN TRẢ] (hoặc đã quá hạn).</p>
                <p>Vui lòng trả sách đúng hạn hoặc gia hạn để tránh phí phạt. Bạn có thể gia hạn tại mục "Sách đang mượn" trong tài khoản của mình.</p>
                <p>Trân trọng,</p>
                <p>Thư viện Số UEF</p>
                """, "format", "plain") // Ví dụ: template này có thể là plain text
        );
    }
}