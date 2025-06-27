package com.uef.library.controller;
import com.uef.library.model.User;
import com.uef.library.service.StringeeCallService;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/notifications")
public class AutoCallController {

    @Autowired
    private UserService userService;
    @Autowired
    private StringeeCallService stringeeCallService;

    @Value("${stringee.webhook.url}")
    private String stringeeWebhookUrl;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auto-call")
    public String showAutoCallPage(Model model, HttpSession session) {
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
        model.addAttribute("scriptTemplates", getCallScriptTemplates());

        return "admin/auto_call_notification";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto-call/send")
    public String handleAutoCallSend(@RequestParam String targetType,
                                     @RequestParam(required = false) String role,
                                     @RequestParam(required = false) String username,
                                     @RequestParam String scriptContent,
                                     RedirectAttributes redirectAttributes) {
        try {
            List<User> recipients;
            if ("all".equals(targetType)) {
                recipients = userService.findAllUsers().stream()
                        .filter(user -> !"ADMIN".equals(user.getRole()))
                        .collect(Collectors.toList());
            } else if ("role".equals(targetType) && StringUtils.hasText(role)) {
                recipients = userService.findByRole(role.toUpperCase());
            } else if ("single".equals(targetType) && StringUtils.hasText(username)) {
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
                recipients = List.of(user);
            } else {
                throw new IllegalArgumentException("Mục tiêu gửi không hợp lệ.");
            }

            int callsInitiated = 0;
            for (User recipient : recipients) {
                if (recipient.getUserDetail() != null && StringUtils.hasText(recipient.getUserDetail().getPhone())) {
                    String phoneNumber = recipient.getUserDetail().getPhone();
                    if (phoneNumber.startsWith("0") && phoneNumber.length() >= 9) {
                        phoneNumber = "84" + phoneNumber.substring(1);
                    }
                    stringeeCallService.makeOutboundCall(phoneNumber, stringeeWebhookUrl, scriptContent);
                    callsInitiated++;
                } else {
                    System.err.println("Skipping call for user " + recipient.getUsername() + ": No valid phone number.");
                }
            }

            if (callsInitiated > 0) {
                redirectAttributes.addFlashAttribute("successMessageAutoCall", "Đã khởi tạo " + callsInitiated + " cuộc gọi thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessageAutoCall", "Không có cuộc gọi nào được khởi tạo. Vui lòng kiểm tra thông tin người nhận và số điện thoại.");
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessageAutoCall", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessageAutoCall", "Đã xảy ra lỗi không mong muốn khi thực hiện cuộc gọi: " + e.getMessage());
        }
        return "redirect:/admin/notifications/auto-call";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CallScriptTemplate {
        private String id;
        private String name;
        private String script;
    }

    private List<CallScriptTemplate> getCallScriptTemplates() {
        return List.of(
                new CallScriptTemplate("welcome_library", "Chào mừng thư viện", "Xin chào quý khách. Đây là cuộc gọi tự động từ Thư viện Số UEF. Cảm ơn quý khách đã sử dụng dịch vụ của chúng tôi."),
                new CallScriptTemplate("overdue_reminder", "Nhắc nhở quá hạn", "Xin chào, đây là thông báo tự động từ Thư viện Số UEF. Sách của bạn đã quá hạn trả. Vui lòng trả sách sớm nhất có thể."),
                new CallScriptTemplate("event_invitation", "Mời tham gia sự kiện", "Thư viện Số UEF trân trọng mời quý khách tham gia sự kiện đọc sách trực tuyến vào lúc tám giờ tối ngày ba mươi tháng bảy. Hãy tham gia cùng chúng tôi."),
                new CallScriptTemplate("new_policy_update", "Cập nhật quy định mới", "Kính gửi quý khách hàng, Thư viện Số UEF vừa cập nhật một số quy định mới. Vui lòng truy cập website để xem chi tiết. Xin cảm ơn."),
                new CallScriptTemplate("system_maintenance", "Bảo trì hệ thống", "Thư viện Số UEF sẽ tạm ngừng hoạt động để bảo trì hệ thống vào lúc nửa đêm. Xin lỗi vì sự bất tiện này. Quý khách vui lòng truy cập lại sau."),
                new CallScriptTemplate("book_recommendation", "Gợi ý sách mới", "Chào mừng bạn đến với Thư viện Số UEF. Chúng tôi có một số đầu sách mới rất hay mà bạn có thể quan tâm. Hãy truy cập website để khám phá ngay!")
        );
    }
}