package com.uef.library.service;

import com.uef.library.model.MarqueeMessage; // cite: MarqueeMessage.java
import com.uef.library.model.MarqueeTemplate; // Import lớp DTO mới
import com.uef.library.repository.MarqueeMessageRepository; // cite: MarqueeMessageRepository.java
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarqueeNotificationService {

    private final MarqueeMessageRepository marqueeMessageRepository; // cite: MarqueeMessageRepository.java

    @PostConstruct
    public void init() {
        // Đảm bảo MarqueeMessage khởi tạo với ID 1L nếu chưa có
        // Đây là bản ghi chính được hiển thị trên frontend
        if (marqueeMessageRepository.count() == 0) {
            MarqueeMessage defaultMessage = new MarqueeMessage(); // cite: MarqueeMessage.java
            defaultMessage.setId(1L); // Đảm bảo ID là 1L
            defaultMessage.setContent("Chào mừng đến với Thư viện Số UEF! Khám phá kho tàng tri thức vô tận của chúng tôi."); // Nội dung mặc định ban đầu
            defaultMessage.setEnabled(true); // Mặc định là bật
            marqueeMessageRepository.save(defaultMessage); // cite: MarqueeMessageRepository.java
        }
        // Các mẫu sẽ được hardcode trong phương thức getMarqueeTemplates()
    }

    @Transactional(readOnly = true)
    public Optional<MarqueeMessage> getCurrentMarqueeMessage() {
        return marqueeMessageRepository.findById(1L); // cite: MarqueeMessageRepository.java
    }

    @Transactional
    public void updateMarqueeMessage(String content, boolean enabled) {
        MarqueeMessage message = marqueeMessageRepository.findById(1L) // cite: MarqueeMessageRepository.java
                .orElseGet(MarqueeMessage::new); // cite: MarqueeMessage.java
        message.setId(1L); // Đảm bảo luôn cập nhật bản ghi có ID là 1L
        message.setContent(content); // cite: MarqueeMessage.java
        message.setEnabled(enabled); // cite: MarqueeMessage.java
        marqueeMessageRepository.save(message); // cite: MarqueeMessageRepository.java
    }

    // === PHƯƠNG THỨC MỚI ĐỂ LẤY DANH SÁCH MẪU MARQUEE (HARDCODE) ===
    public List<MarqueeTemplate> getMarqueeTemplates() {
        return List.of(
                new MarqueeTemplate("event_2025", "Sự kiện: Tuần lễ sách 2025", "⚡ TUẦN LỄ SÁCH 2025 - Cơ hội vàng sở hữu sách hay giá cực tốt! Đừng bỏ lỡ các buổi tọa đàm thú vị! Chi tiết tại website."),
                new MarqueeTemplate("maintenance_alert", "Bảo trì hệ thống", "🛠️ Hệ thống Thư viện Số sẽ bảo trì từ 00:00 - 02:00 ngày [NGÀY]. Vui lòng sao lưu dữ liệu cần thiết. Mong quý vị thông cảm!"),
                new MarqueeTemplate("new_policy", "Cập nhật Quy định mới", "🔔 THÔNG BÁO QUAN TRỌNG: Quy định mượn/trả sách mới có hiệu lực từ [NGÀY]. Đọc chi tiết tại mục Giới thiệu."),
                new MarqueeTemplate("recruitment_call", "Tuyển cộng tác viên", "🌟 Thư viện đang tìm kiếm cộng tác viên yêu sách! Tham gia cùng chúng tôi để lan tỏa tri thức. Đăng ký ngay!"),
                new MarqueeTemplate("general_welcome", "Chào mừng độc giả", "📚 Chào mừng quý độc giả đến với Thư viện Số UEF! Khám phá kho tàng tri thức vô tận và tận hưởng niềm vui đọc sách mỗi ngày!"),
                new MarqueeTemplate("promotion_spring", "Ưu đãi đặc biệt", "🎁 Đừng bỏ lỡ chương trình Ưu Đãi Mùa Xuân! Hàng ngàn đầu sách giảm giá đến 50%. Duy nhất trong tháng [THÁNG]. Mượn ngay!")
        );
    }
}