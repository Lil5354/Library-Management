package com.uef.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mỗi thông báo sẽ thuộc về một người dùng cụ thể
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient; // Người nhận

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message; // Nội dung thông báo

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; // Trạng thái đã đọc (mặc định là chưa đọc)

    @Column(length = 255)
    private String link; // (Tùy chọn) Đường dẫn khi nhấp vào thông báo

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo


    public enum NotificationTargetType {
        ALL_USERS,
        BY_ROLE,
        SINGLE_USER
    }
}
