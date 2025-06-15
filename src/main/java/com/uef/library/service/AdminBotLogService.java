package com.uef.library.service;
import com.uef.library.model.AdminBotLog;
import com.uef.library.repository.AdminBotLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminBotLogService {

    @Autowired
    private AdminBotLogRepository adminBotLogRepository;

    public List<AdminBotLog> getAllLogs() {
        return adminBotLogRepository.findTop50ByOrderByCreatedAtDesc();
    }


    public void saveLog(String message) {
        AdminBotLog log = new AdminBotLog();
        log.setMessage(message);
        adminBotLogRepository.save(log);
    }
}