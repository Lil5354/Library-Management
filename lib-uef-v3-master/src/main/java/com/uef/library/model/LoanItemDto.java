package com.uef.library.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

@Data
public class LoanItemDto {
    private long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverImage;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private String status;
    private double lateFee;
    private boolean hasReviewed;
    // Constructor để chuyển đổi từ Entity sang DTO
    public LoanItemDto(LoanItem loanItem) {
        Book book = loanItem.getBook();
        BookLoan loan = loanItem.getBookLoan();

        this.bookId = book.getId();
        this.bookTitle = book.getTitle();
        this.bookAuthor = book.getAuthor();
        this.bookCoverImage = book.getCoverImage();

        this.borrowDate = loan.getBorrowDate();
        this.dueDate = loan.getDueDate();
        this.returnDate = loanItem.getReturnDate();
        this.status = loanItem.getStatus();

        // Tính phí phạt
        this.lateFee = calculateLateFee(loanItem);
    }

    private double calculateLateFee(LoanItem loanItem) {
        final double LATE_FEE_PER_DAY = 2000; // Phí phạt từ đồ án 1

        // Nếu đã trả sách và ngày trả sau ngày hẹn trả
        if (loanItem.getReturnDate() != null && loanItem.getReturnDate().toLocalDate().isAfter(loanItem.getBookLoan().getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(loanItem.getBookLoan().getDueDate(), loanItem.getReturnDate().toLocalDate());
            return daysOverdue * LATE_FEE_PER_DAY;
        }

        // Nếu chưa trả sách và hôm nay đã quá ngày hẹn trả
        if (loanItem.getReturnDate() == null && LocalDate.now().isAfter(loanItem.getBookLoan().getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(loanItem.getBookLoan().getDueDate(), LocalDate.now());
            return daysOverdue * LATE_FEE_PER_DAY;
        }

        return 0; // Không có phí phạt
    }
}