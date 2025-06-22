package com.uef.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StringeeCallService {

    @Value("${stringee.from.number}")
    private String stringeeFromNumber;

    @Autowired
    private StringeeAuthService stringeeAuthService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getStringeeFromNumber() {
        return stringeeFromNumber;
    }

    public void makeOutboundCall(String toPhoneNumber, String webhookUrl, String scriptContent) {
        String stringeeApiUrl = "https://api.stringee.com/v1/call2/callout";
        String accessToken = stringeeAuthService.generateAccessToken();
        if (accessToken == null) {
            System.err.println("Failed to make call, Access Token is null.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-STRINGEE-AUTH", accessToken);

        List<Map<String, Object>> actions = new ArrayList<>();
        actions.add(Map.of("action", "talk", "text", scriptContent, "voice", "female"));
        actions.add(Map.of("action", "hangup"));

        Map<String, Object> from = Map.of("type", "external", "number", stringeeFromNumber, "alias", "Thu vien UEF Bot");
        List<Map<String, Object>> to = List.of(Map.of("type", "external", "number", toPhoneNumber, "alias", toPhoneNumber));
        Map<String, Object> body = new HashMap<>();
        body.put("from", from);
        body.put("to", to);
        body.put("actions", actions);
        body.put("event_url", webhookUrl);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            System.out.println("Calling Stringee API with dynamic script in payload: " + jsonBody);

            ResponseEntity<String> response = restTemplate.postForEntity(stringeeApiUrl, requestEntity, String.class);
            System.out.println("Stringee API Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error making outbound call: " + e.getMessage());
        }
    }
}