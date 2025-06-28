package com.uef.library.controller;

import com.uef.library.dto.ReviewDTO;
import com.uef.library.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<?> submitReview(@RequestBody ReviewDTO reviewDto, Authentication authentication) {
        try {
            reviewService.createReview(reviewDto, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã gửi đánh giá!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBookId(@PathVariable Long bookId) {
        List<ReviewDTO> reviews = reviewService.getReviewsForBook(bookId);
        return ResponseEntity.ok(reviews);
    }
}