package com.uef.library.controller;

import com.uef.library.model.Notification;
import com.uef.library.model.User;
import com.uef.library.service.NotificationService;
import com.uef.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // Helper method to get the current authenticated user
    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.findByUsername(username).orElse(null);
    }

    // API để lấy danh sách thông báo đã phân trang cho người dùng hiện tại
    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationService.getNotificationsForUser(user, pageable);
        return ResponseEntity.ok(notifications);
    }

    // API: Lấy số lượng thông báo chưa đọc cho người dùng hiện tại
    @GetMapping("/count-unread")
    public ResponseEntity<Long> getUnreadNotificationCount(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        long count = notificationService.getUnreadNotificationCount(user);
        return ResponseEntity.ok(count);
    }

    // API: Đánh dấu một thông báo cụ thể là đã đọc
    @PostMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markSingleNotificationAsRead(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = notificationService.markSingleAsRead(id, user);
        return success ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden nếu không thể đánh dấu (không tìm thấy hoặc không thuộc về user)
    }

    // API để đánh dấu tất cả là đã đọc
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    // API để xóa một thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean deleted = notificationService.deleteNotification(id, user);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden nếu cố xóa của người khác
    }
}