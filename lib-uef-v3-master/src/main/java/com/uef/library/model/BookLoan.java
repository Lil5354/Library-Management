package com.uef.library.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "book_loans")
@Getter @Setter
public class BookLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime borrowDate;

    private LocalDateTime dueDate;

    @OneToMany(mappedBy = "bookLoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LoanItem> loanItems;

    private String status; // Ví dụ: ACTIVE, COMPLETED
}