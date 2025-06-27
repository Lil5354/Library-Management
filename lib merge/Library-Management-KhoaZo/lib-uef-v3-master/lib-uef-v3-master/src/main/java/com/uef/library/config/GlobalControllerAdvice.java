package com.uef.library.config;

import com.uef.library.model.MarqueeMessage;
import com.uef.library.model.Notification;
import com.uef.library.model.User;
import com.uef.library.service.MarqueeNotificationService;
import com.uef.library.service.NotificationService;
import com.uef.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private  MarqueeNotificationService marqueeNotificationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    public GlobalControllerAdvice(UserService userService, MarqueeNotificationService marqueeNotificationService) {
        this.userService = userService;
        this.marqueeNotificationService = marqueeNotificationService;
    }

    @ModelAttribute("loggedInUserFullName")
    public String getLoggedInUserFullName(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username;

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            } else {
                return "Bạn"; // Giá trị mặc định
            }

            if (username != null) {
                User user = userService.findByUsername(username).orElse(null);
                if (user != null && user.getUserDetail() != null) {
                    String fetchedName = user.getUserDetail().getFullName();
                    if (fetchedName != null && !fetchedName.trim().isEmpty() && !fetchedName.equalsIgnoreCase("Chưa cập nhật")) {
                        return fetchedName;
                    }
                }
                return username; // Trả về username nếu không có fullName
            }
        }
        return "Bạn"; // Giá trị mặc định
    }

    @ModelAttribute("loggedInUserEmail")
    public String getLoggedInUserEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String usernameOrEmail = null;

            if (principal instanceof UserDetails) {
                usernameOrEmail = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                usernameOrEmail = (String) principal;
            }

            if (usernameOrEmail != null) {
                User user = userService.findByUsername(usernameOrEmail).orElse(null);
                // Sửa lỗi: Truy cập email từ UserDetail
                if (user != null && user.getUserDetail() != null && user.getUserDetail().getEmail() != null && !user.getUserDetail().getEmail().isEmpty()) {
                    return user.getUserDetail().getEmail();
                }
                // Giữ lại fallback nếu username là email
                if (usernameOrEmail.contains("@")) {
                    return usernameOrEmail;
                }
            }
        }
        return "Không có thông tin email";
    }

    @ModelAttribute("loggedInUserAvatarUrl")
    public String getLoggedInUserAvatarUrl(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = null;

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }

            if (username != null) {
                User user = userService.findByUsername(username).orElse(null);
                // Sửa lỗi: Truy cập avatar từ UserDetail
                if (user != null && user.getUserDetail() != null && user.getUserDetail().getAvatar() != null && !user.getUserDetail().getAvatar().isEmpty()) {
                    // Giả sử getAvatar() trả về đường dẫn URL hoặc tên file avatar
                    // Ví dụ: return "/uploads/avatars/" + user.getUserDetail().getAvatar(); (nếu là tên file)
                    // Hoặc return user.getUserDetail().getAvatar(); (nếu đã là URL đầy đủ)
                    return user.getUserDetail().getAvatar();
                }
            }
        }
        return null; // Trả về null nếu không có avatar, template sẽ hiển thị avatar mặc định
    }

    @ModelAttribute("loggedInUsername")
    public String getLoggedInUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) { // Đôi khi principal chỉ là String username
                return (String) principal;
            }
            // Nếu principal là một đối tượng khác, bạn có thể cần toString() hoặc một cách khác
            // nhưng thường thì UserDetails là trường hợp phổ biến nhất.
            return principal.toString();
        }
        return null; // Hoặc "Guest" nếu muốn có giá trị mặc định khi chưa đăng nhập
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    @ModelAttribute("activeMarqueeMessage")
    public String getActiveMarqueeMessage() {
        return marqueeNotificationService.getCurrentMarqueeMessage()
                .filter(MarqueeMessage::isEnabled) // Chỉ lấy nếu được kích hoạt
                .map(MarqueeMessage::getContent)
                .filter(content -> content != null && !content.trim().isEmpty()) // Chỉ lấy nếu nội dung không rỗng
                .orElse(null); // Trả về null nếu không có thông báo hợp lệ
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                return notificationService.getUnreadNotificationCount(user);
            }
        }
        return 0;
    }

    @ModelAttribute("initialNotifications")
    public List<Notification> getInitialNotifications(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                // Lấy 5 thông báo đầu tiên
                Page<Notification> notificationPage = notificationService.getNotificationsForUser(user, PageRequest.of(0, 5));
                return notificationPage.getContent();
            }
        }
        return Collections.emptyList();
    }
}