package com.uef.library.controller;
import com.uef.library.model.AdminBotLog;
import com.uef.library.service.AdminBotLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/bot-logs")
public class AdminBotLogController {

    @Autowired
    private AdminBotLogService adminBotLogService;

    @PostMapping("/add")
    public ResponseEntity<Void> saveLog(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message != null && !message.isEmpty()) {
            adminBotLogService.saveLog(message);       // Lưu vào CSDL
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/all")
    public List<AdminBotLog> getAllLogs() {
        return adminBotLogService.getAllLogs();
    }
}