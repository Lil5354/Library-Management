package com.uef.library.service;

import com.uef.library.model.MarqueeMessage;
import com.uef.library.repository.MarqueeMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MarqueeNotificationService {

    private final MarqueeMessageRepository marqueeMessageRepository;

    @Autowired
    public MarqueeNotificationService(MarqueeMessageRepository marqueeMessageRepository) {
        this.marqueeMessageRepository = marqueeMessageRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MarqueeMessage> getCurrentMarqueeMessage() {
        return marqueeMessageRepository.findById(1L);
    }

    @Transactional
    public void updateMarqueeMessage(String content, boolean enabled) {
        MarqueeMessage message = marqueeMessageRepository.findById(1L)
                .orElseGet(() -> {
                    MarqueeMessage newMessage = new MarqueeMessage();
                    return newMessage;
                });
        message.setContent(content);
        message.setEnabled(enabled);
        marqueeMessageRepository.save(message);
    }
}