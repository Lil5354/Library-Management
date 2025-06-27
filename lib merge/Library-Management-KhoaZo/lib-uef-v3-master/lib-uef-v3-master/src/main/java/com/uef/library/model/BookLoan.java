package com.uef.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book_loans")
@Getter
@Setter
public class BookLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "borrow_date", nullable = false, updatable = false)
    private LocalDateTime borrowDate;

    // <<< SỬA ĐỔI: Chuyển từ LocalDateTime sang LocalDate cho phù hợp nghiệp vụ >>>
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @OneToMany(mappedBy = "bookLoan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LoanItem> loanItems = new ArrayList<>();

    @Column(length = 20)
    private String status; // Ví dụ: ACTIVE, COMPLETED

    @Column(name = "renewal_count", nullable = false)
    private int renewalCount = 0;

    public void addLoanItem(LoanItem item) {
        loanItems.add(item);
        item.setBookLoan(this);
    }
}