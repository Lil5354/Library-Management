// src/main/java/com/uef.library.service/NotificationService.java
package com.uef.library.service;

import com.uef.library.model.Notification;
import com.uef.library.model.NotificationTemplate; // Import lớp DTO mới
import com.uef.library.model.User;
import com.uef.library.repository.NotificationRepository;
import com.uef.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime; // Import cho LocalDateTime
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean sendNotification(Notification.NotificationTargetType type, String targetIdentifier, String title, String message, String link) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Tiêu đề thông báo không được để trống.");
        }
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Nội dung thông báo không được để trống.");
        }

        List<User> recipients;

        switch (type) {
            case ALL_USERS:
                recipients = userRepository.findAll().stream()
                        .filter(user -> !"ADMIN".equals(user.getRole()))
                        .toList();
                break;
            case BY_ROLE:
                if (!StringUtils.hasText(targetIdentifier)) {
                    throw new IllegalArgumentException("Phải chọn vai trò khi gửi thông báo theo nhóm.");
                }
                recipients = userRepository.findByRole(targetIdentifier.toUpperCase());
                break;
            case SINGLE_USER:
                if (!StringUtils.hasText(targetIdentifier)) {
                    throw new IllegalArgumentException("Phải nhập username khi gửi cho người dùng đơn lẻ.");
                }
                Optional<User> userOpt = userRepository.findByUsername(targetIdentifier);
                if (userOpt.isEmpty()) {
                    throw new IllegalArgumentException("Không tìm thấy người dùng với username: " + targetIdentifier);
                }
                recipients = Collections.singletonList(userOpt.get());
                break;
            default:
                throw new IllegalArgumentException("Loại mục tiêu không hợp lệ.");
        }

        for (User recipient : recipients) {
            createNotification(recipient, title, message, link);
        }
        return true;
    }

    private void createNotification(User recipient, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(StringUtils.hasText(link) ? link : null);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now()); // Thêm dòng này để set thời gian tạo
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(User user, Pageable pageable) {
        return notificationRepository.findByRecipient(user, pageable);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public boolean deleteNotification(Long notificationId, User user) {
        return notificationRepository.deleteByIdAndRecipient(notificationId, user) > 0;
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User user) {
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    // === PHƯƠNG THỨC LẤY DANH SÁCH MẪU THÔNG BÁO NGƯỜI DÙNG (HARDCODE) ===
    public List<NotificationTemplate> getUserNotificationTemplates() {
        return List.of(
                new NotificationTemplate("book_due_soon", "Nhắc nhở Sách sắp đến hạn", "Sắp đến hạn trả sách", "Sách \"Lập trình Java Nâng Cao\" của bạn sẽ đến hạn trả vào ngày 25/06/2025. Vui lòng trả sách đúng hạn hoặc gia hạn để tránh phí phạt."),
                new NotificationTemplate("reserved_book_ready", "Thông báo Sách đặt trước đã có", "Sách đặt trước đã có", "Sách \"Khoa học dữ liệu cơ bản\" mà bạn đặt trước đã có sẵn tại quầy thư viện. Vui lòng đến nhận trong vòng 3 ngày."),
                new NotificationTemplate("new_workshop", "Mời tham gia Workshop", "Mời tham gia Workshop: Kỹ năng đọc hiệu quả", "Thư viện tổ chức workshop \"Kỹ năng đọc hiệu quả\" vào lúc 09:00 ngày 10/07/2025. Đăng ký ngay để nâng cao kỹ năng đọc của bạn!"),
                new NotificationTemplate("account_issue", "Tài khoản thư viện có vấn đề", "Tài khoản thư viện của bạn có vấn đề", "Tài khoản của bạn đã bị khóa tạm thời do sách \"Lịch sử Việt Nam\" quá hạn. Vui lòng liên hệ thủ thư để giải quyết."),
                new NotificationTemplate("general_thank_you", "Cảm ơn bạn đã sử dụng Thư viện Số", "Cảm ơn bạn đã sử dụng Thư viện Số", "Cảm ơn bạn đã đồng hành cùng Thư viện Số UEF. Chúng tôi luôn cố gắng nâng cao chất lượng dịch vụ để phục vụ bạn tốt nhất."),
                new NotificationTemplate("new_book_favorite_category", "Sách mới về trong danh mục yêu thích", "Sách mới về trong danh mục yêu thích của bạn!", "Một cuốn sách mới thuộc thể loại \"Tiểu thuyết\" mà bạn quan tâm đã được thêm vào thư viện: \"Đắc nhân tâm\". Khám phá ngay!")
        );
    }
}