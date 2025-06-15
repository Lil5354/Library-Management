package com.uef.library.service;

import com.uef.library.model.Notification;
import com.uef.library.model.User;
import com.uef.library.repository.NotificationRepository;
import com.uef.library.repository.UserRepository; // Đảm bảo đã import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.uef.library.model.Notification.NotificationTargetType.*;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Phương thức chính để gửi thông báo, logic tập trung tại đây.
     * @param type Loại mục tiêu (tất cả, theo vai trò, người dùng đơn lẻ).
     * @param targetIdentifier Tên vai trò hoặc username, tùy thuộc vào type.
     * @param message Nội dung thông báo.
     * @param link Đường dẫn đính kèm (tùy chọn).
     * @return true nếu gửi thành công, false nếu có lỗi.
     * @throws IllegalArgumentException nếu thông tin không hợp lệ.
     */
    @Transactional
    // Thay đổi 1: Thêm 'String title' vào phương thức
    public boolean sendNotification(Notification.NotificationTargetType type, String targetIdentifier, String title, String message, String link) {
        // Thay đổi 2: Thêm kiểm tra cho title
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Tiêu đề thông báo không được để trống.");
        }
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Nội dung thông báo không được để trống.");
        }

        List<User> recipients;

        // ... (phần switch case giữ nguyên)
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
            // Thay đổi 3: Truyền 'title' vào phương thức createNotification
            createNotification(recipient, title, message, link);
        }
        return true;
    }

    private void createNotification(User recipient, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        // Thay đổi 5: Gán giá trị cho title
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(StringUtils.hasText(link) ? link : null);
        notification.setRead(false);
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
        // Chỉ xóa nếu thông báo thuộc về đúng người dùng
        return notificationRepository.deleteByIdAndRecipient(notificationId, user) > 0;
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User user) {
        if (user == null) return 0;
        // Phương thức này sử dụng hàm đã có sẵn trong NotificationRepository
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

}