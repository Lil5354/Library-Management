package com.uef.library.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoanItemDTO {
    private Long itemId;
    private String bookTitle;
    private String bookIsbn;
    private String status; // BORROWED, RETURNED
    private LocalDateTime returnDate;
}