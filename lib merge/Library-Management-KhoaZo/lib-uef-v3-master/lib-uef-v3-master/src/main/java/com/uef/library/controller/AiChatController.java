package com.uef.library.controller;

import com.uef.library.service.ChatPromptService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final ChatModel chatModel;
    private final ChatPromptService promptService;

    @Autowired
    public AiChatController(ChatModel chatModel, ChatPromptService promptService) {
        this.chatModel = chatModel;
        this.promptService = promptService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChatMessage(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String finalPrompt = promptService.createFinalPrompt(userMessage);

        String botResponse = chatModel.call(finalPrompt);

        return ResponseEntity.ok(Map.of("reply", botResponse));
    }
}