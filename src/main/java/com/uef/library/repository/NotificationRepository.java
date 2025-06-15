package com.uef.library.repository;

import com.uef.library.model.Notification;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy danh sách thông báo cho một người dùng, sắp xếp theo thời gian mới nhất
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    // Đếm số thông báo chưa đọc của một người dùng
    long countByRecipientAndIsReadFalse(User recipient);

    Page<Notification> findByRecipient(User user, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :user AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user);

    long deleteByIdAndRecipient(Long id, User recipient);


}