package com.uef.library.service;

import com.uef.library.model.BookLoan;
import com.uef.library.model.LoanItem;
import com.uef.library.model.Notification;
import com.uef.library.model.User;
import com.uef.library.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverdueReminderService {

    private final BookLoanRepository bookLoanRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine; // Thêm TemplateEngine để render HTML email

    // Lên lịch chạy mỗi ngày vào lúc 8:00 sáng
    // Cron expression: second, minute, hour, day of month, month, day of week
    // "0 0 8 * * *" nghĩa là 0 giây, 0 phút, 8 giờ, mỗi ngày, mỗi tháng, mỗi thứ trong tuần
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendOverdueReminders() {
        LocalDate today = LocalDate.now();
        System.out.println("Bắt đầu kiểm tra sách quá hạn để gửi nhắc nhở tự động vào lúc: " + LocalDate.now());

        // Lấy tất cả các LoanItem đang ở trạng thái 'BORROWED' và có hạn trả trước ngày hiện tại
        List<BookLoan> overdueBookLoans = bookLoanRepository.findActiveOverdueBookLoans(today);

        if (overdueBookLoans.isEmpty()) {
            System.out.println("Không có phiếu mượn nào quá hạn để gửi nhắc nhở.");
            return;
        }

        System.out.println("Tìm thấy " + overdueBookLoans.size() + " phiếu mượn quá hạn. Đang tiến hành gửi email...");

        for (BookLoan loan : overdueBookLoans) {
            sendEmailForOverdueLoan(loan); // Sử dụng phương thức dùng chung
        }
        System.out.println("Kết thúc kiểm tra sách quá hạn.");
    }

    /**
     * Gửi nhắc nhở email cho một phiếu mượn cụ thể.
     * Thường dùng cho việc kích hoạt thủ công.
     * @param loanId ID của phiếu mượn.
     * @throws Exception nếu không tìm thấy phiếu mượn hoặc không thể gửi email.
     */
    @Transactional
    public void sendSingleOverdueReminder(Long loanId) throws Exception {
        BookLoan loan = bookLoanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Không tìm thấy phiếu mượn với ID: " + loanId));

        LocalDate today = LocalDate.now();
        List<LoanItem> overdueItemsInThisLoan = loan.getLoanItems().stream()
                .filter(item -> "BORROWED".equals(item.getStatus()) && loan.getDueDate().isBefore(today))
                .collect(Collectors.toList());

        if (overdueItemsInThisLoan.isEmpty()) {
            throw new Exception("Phiếu mượn ID " + loanId + " không có sách nào quá hạn để gửi nhắc nhở.");
        }

        sendEmailForOverdueLoan(loan);
    }

    // Phương thức trợ giúp để gửi email và tạo thông báo cho một phiếu mượn cụ thể
    private void sendEmailForOverdueLoan(BookLoan loan) {
        User reader = loan.getUser();
        LocalDate today = LocalDate.now();
        List<LoanItem> overdueItemsInThisLoan = loan.getLoanItems().stream()
                .filter(item -> "BORROWED".equals(item.getStatus()) && loan.getDueDate().isBefore(today))
                .collect(Collectors.toList());

        if (reader.getUserDetail() != null && reader.getUserDetail().getEmail() != null && !reader.getUserDetail().getEmail().isEmpty() && !overdueItemsInThisLoan.isEmpty()) {
            String recipientEmail = reader.getUserDetail().getEmail();
            String subject = "[THƯ VIỆN SỐ] NHẮC NHỞ SÁCH QUÁ HẠN - Mã phiếu: " + loan.getId();

            // Tạo context cho Thymeleaf template
            Context context = new Context();
            context.setVariable("readerName", reader.getUserDetail().getFullName());
            context.setVariable("loanId", loan.getId());
            context.setVariable("dueDate", loan.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("overdueDays", ChronoUnit.DAYS.between(loan.getDueDate(), today));
            context.setVariable("overdueItems", overdueItemsInThisLoan.stream().map(item -> new OverdueBookInfo(
                    item.getBook().getTitle(),
                    item.getBook().getIsbn()
            )).collect(Collectors.toList()));


            // Render template HTML email
            String htmlContent = templateEngine.process("email/overdue_reminder", context);

            try {
                emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);

                // Lưu thông báo vào hệ thống Notification
                String notificationTitle = "Nhắc nhở sách quá hạn!";
                String notificationMessage = String.format("Bạn có sách quá hạn trả. Vui lòng kiểm tra email của bạn để biết chi tiết. Mã phiếu: %d", loan.getId());
                notificationService.sendNotification(
                        Notification.NotificationTargetType.SINGLE_USER,
                        reader.getUsername(),
                        notificationTitle,
                        notificationMessage,
                        "/reader/borrowed-books" // Link đến trang sách đang mượn của độc giả
                );
                System.out.println("Đã gửi email nhắc nhở và tạo thông báo cho độc giả: " + reader.getUserDetail().getFullName() + " (" + recipientEmail + ")");
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email nhắc nhở cho " + recipientEmail + ": " + e.getMessage());
            }
        } else {
            System.out.println("Bỏ qua độc giả " + reader.getUserId() + " (Email không hợp lệ hoặc không có sách quá hạn trong danh sách lọc).");
        }
    }

    // Class helper để truyền dữ liệu vào template
    public static class OverdueBookInfo {
        private String title;
        private String isbn;

        public OverdueBookInfo(String title, String isbn) {
            this.title = title;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getIsbn() {
            return isbn;
        }
    }
}