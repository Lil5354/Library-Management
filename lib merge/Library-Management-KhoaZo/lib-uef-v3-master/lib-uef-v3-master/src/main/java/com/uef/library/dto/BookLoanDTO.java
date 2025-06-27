package com.uef.library.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookLoanDTO {
    private Long loanId;
    private String readerName;
    private String readerId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private String status; // ACTIVE, COMPLETED
    private int renewalCount;
    private List<LoanItemDTO> items;

    // Thông tin về quá hạn và phí phạt
    private long overdueDays;
    private double fineAmount;
}