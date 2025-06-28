package com.uef.library.service;

import com.uef.library.model.Notification;
import com.uef.library.model.NotificationTemplate;
import com.uef.library.model.User;
import com.uef.library.repository.NotificationRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Gửi thông báo đến các đối tượng được chỉ định.
     *
     * @param type             Loại đối tượng (ALL_USERS, BY_ROLE, SINGLE_USER).
     * @param targetIdentifier Định danh của đối tượng (tên vai trò hoặc username).
     * @param title            Tiêu đề thông báo.
     * @param message          Nội dung thông báo.
     * @param link             Đường dẫn tùy chọn.
     * @return true nếu gửi thành công.
     */
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
                // Gửi cho tất cả người dùng không phải là ADMIN
                recipients = userRepository.findAll().stream()
                        .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                        .toList();
                break;
            case BY_ROLE:
                if (!StringUtils.hasText(targetIdentifier)) {
                    throw new IllegalArgumentException("Phải chọn vai trò khi gửi thông báo theo nhóm.");
                }
                // Gửi cho tất cả người dùng trong một vai trò cụ thể (READER hoặc STAFF)
                recipients = userRepository.findByRole(targetIdentifier.toUpperCase());
                break;
            case SINGLE_USER:
                if (!StringUtils.hasText(targetIdentifier)) {
                    throw new IllegalArgumentException("Phải nhập username khi gửi cho người dùng đơn lẻ.");
                }
                User user = userRepository.findByUsername(targetIdentifier)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + targetIdentifier));
                recipients = Collections.singletonList(user);
                break;
            default:
                throw new IllegalArgumentException("Loại mục tiêu không hợp lệ.");
        }

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy người dùng nào phù hợp với mục tiêu đã chọn.");
        }

        for (User recipient : recipients) {
            createNotification(recipient, title, message, link);
        }
        return true;
    }

    /**
     * Tạo và lưu một bản ghi thông báo mới cho người nhận.
     */
    private void createNotification(User recipient, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(StringUtils.hasText(link) ? link : null);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(User user, Pageable pageable) {
        if (user == null) {
            return Page.empty(pageable);
        }
        return notificationRepository.findByRecipient(user, pageable);
    }

    @Transactional
    public void markAllAsRead(User user) {
        if (user == null) return;
        notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public boolean deleteNotification(Long notificationId, User user) {
        if (user == null) return false;
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // CHỈ XÓA NẾU THÔNG BÁO THUỘC VỀ NGƯỜI DÙNG HIỆN TẠI
            if (notification.getRecipient() != null && notification.getRecipient().getUserId().equals(user.getUserId())) {
                notificationRepository.deleteById(notificationId);
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User user) {
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    // PHƯƠNG THỨC NÀY ĐÃ ĐƯỢC SỬA LỖI LOGIC QUYỀN SỞ HỮU
    @Transactional
    public boolean markSingleAsRead(Long notificationId, User user) {
        if (user == null) return false;
        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (notifOpt.isPresent()) {
            Notification notification = notifOpt.get();
            // CHỈ ĐÁNH DẤU LÀ ĐÃ ĐỌC NẾU THÔNG BÁO THUỘC VỀ NGƯỜI NHẬN HIỆN TẠI VÀ CHƯA ĐỌC
            if (notification.getRecipient() != null && notification.getRecipient().getUserId().equals(user.getUserId()) && !notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
                return true;
            }
        }
        return false;
    }

    // Lấy danh sách mẫu thông báo (hardcode)
    public List<NotificationTemplate> getUserNotificationTemplates() {
        return List.of(
                new NotificationTemplate("book_due_soon", "Nhắc nhở Sách sắp đến hạn", "Sắp đến hạn trả sách", "Sách \"[TÊN SÁCH]\" của bạn sẽ đến hạn trả vào ngày [NGÀY]. Vui lòng trả sách đúng hạn hoặc gia hạn để tránh phí phạt."),
                new NotificationTemplate("reserved_book_ready", "Thông báo Sách đặt trước đã có", "Sách đặt trước đã có", "Sách \"[TÊN SÁCH]\" mà bạn đặt trước đã có sẵn tại quầy thư viện. Vui lòng đến nhận trong vòng 3 ngày."),
                new NotificationTemplate("new_workshop", "Mời tham gia Workshop", "Mời tham gia Workshop: [TÊN WORKSHOP]", "Thư viện tổ chức workshop \"[TÊN WORKSHOP]\" vào lúc [GIỜ] ngày [NGÀY]. Đăng ký ngay để nâng cao kỹ năng của bạn!"),
                new NotificationTemplate("account_issue", "Tài khoản thư viện có vấn đề", "Tài khoản thư viện của bạn có vấn đề", "Tài khoản của bạn đã bị khóa tạm thời do sách \"[TÊN SÁCH]\" quá hạn. Vui lòng liên hệ thủ thư để giải quyết."),
                new NotificationTemplate("general_thank_you", "Cảm ơn bạn đã sử dụng Thư viện Số", "Cảm ơn bạn đã sử dụng Thư viện Số", "Cảm ơn bạn đã đồng hành cùng Thư viện Số UEF. Chúng tôi luôn cố gắng nâng cao chất lượng dịch vụ để phục vụ bạn tốt nhất."),
                new NotificationTemplate("new_book_favorite_category", "Sách mới về trong danh mục yêu thích", "Sách mới về trong danh mục yêu thích của bạn!", "Một cuốn sách mới thuộc thể loại \"[TÊN THỂ LOẠI]\" mà bạn quan tâm đã được thêm vào thư viện: \"[TÊN SÁCH]\". Khám phá ngay!")
        );
    }
}