package com.uef.library.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stringee-webhook")
public class StringeeWebhookController {

    @PostMapping
    public ResponseEntity<String> handleEventWebhook(@RequestBody(required = false) String payload) {
        if (payload != null) {
            System.out.println("Received Stringee Event Webhook: " + payload);
        }
        return ResponseEntity.ok("Webhook received and processed.");
    }
}