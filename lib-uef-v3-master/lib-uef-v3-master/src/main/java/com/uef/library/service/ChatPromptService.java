package com.uef.library.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPromptService {

    private final TrainAiService trainAiService;
    private String systemPromptTemplate;

    @PostConstruct
    public void init() {
        this.systemPromptTemplate = trainAiService.getActiveKnowledgeContent();
    }

    public void reloadKnowledge() {
        this.systemPromptTemplate = trainAiService.getActiveKnowledgeContent();
    }

    public String createFinalPrompt(String userMessage) {
        return this.systemPromptTemplate + "\n\nHỏi: " + userMessage + "\nTrả lời:";
    }

    public String getCurrentPersonaName() {
        return trainAiService.getActivePersonaName();
    }
}