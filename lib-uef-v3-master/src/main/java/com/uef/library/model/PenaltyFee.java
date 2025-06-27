package com.uef.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalty_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === THAY ĐỔI: Liên kết với LoanItem thay vì Borrowing ===
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_item_id", nullable = false, unique = true)
    private LoanItem loanItem;
    // =======================================================

    // Giữ lại các trường này để dễ dàng truy vấn mà không cần join nếu không cần thiết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "penalty_amount", nullable = false)
    private Double penaltyAmount;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PenaltyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "waived_reason")
    private String waivedReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by_user_id")
    private User collectedByUser;
}