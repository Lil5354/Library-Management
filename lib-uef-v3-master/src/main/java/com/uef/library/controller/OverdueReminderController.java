package com.uef.library.controller;

import com.uef.library.service.OverdueReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overdue-reminders")
@RequiredArgsConstructor
public class OverdueReminderController {

    private final OverdueReminderService overdueReminderService;

    @PostMapping("/send-single-reminder/{loanId}")
    public ResponseEntity<?> sendSingleReminder(@PathVariable Long loanId) {
        try {
            overdueReminderService.sendSingleOverdueReminder(loanId);
            return ResponseEntity.ok().body("Đã gửi email nhắc nhở thành công cho phiếu mượn ID: " + loanId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi nhắc nhở: " + e.getMessage());
        }
    }

    // Endpoint này có thể dùng để kích hoạt chạy toàn bộ nhắc nhở thủ công,
    // nhưng thường nên chạy tự động qua @Scheduled
    // @PostMapping("/run-all-reminders")
    // public ResponseEntity<?> runAllReminders() {
    //     overdueReminderService.sendOverdueReminders();
    //     return ResponseEntity.ok().body("Đã kích hoạt quá trình gửi tất cả nhắc nhở quá hạn.");
    // }
}