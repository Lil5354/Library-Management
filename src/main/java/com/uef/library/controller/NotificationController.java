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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // API để lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (authentication == null) return ResponseEntity.status(401).build();

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationService.getNotificationsForUser(user, pageable);
        return ResponseEntity.ok(notifications);
    }

    // API để đánh dấu tất cả là đã đọc
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    // API để xóa một thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        boolean deleted = notificationService.deleteNotification(id, user);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(403).build(); // 403 Forbidden nếu cố xóa của người khác
    }

}