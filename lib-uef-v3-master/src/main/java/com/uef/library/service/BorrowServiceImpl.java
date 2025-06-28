package com.uef.library.service;

import com.uef.library.model.*;
import com.uef.library.repository.BookLoanRepository;
import com.uef.library.repository.BookRepository;
import com.uef.library.repository.LoanItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // <<< NHỚ IMPORT LocalDate
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final LoanItemRepository loanItemRepository;

    private static final int MAX_BORROW_LIMIT = 5;
    private static final int LOAN_DURATION_DAYS = 14;

    @Override
    @Transactional
    public void createLoan(User user, List<Book> booksToBorrow) {
        if (booksToBorrow == null || booksToBorrow.isEmpty()) {
            throw new IllegalStateException("Vui lòng chọn ít nhất một cuốn sách để mượn.");
        }
        long currentBorrowedCount = loanItemRepository.countActiveLoansByUser(user);
        if (currentBorrowedCount + booksToBorrow.size() > MAX_BORROW_LIMIT) {
            throw new IllegalStateException("Bạn chỉ có thể mượn tối đa " + MAX_BORROW_LIMIT + " cuốn sách. Bạn đang mượn " + currentBorrowedCount + " cuốn.");
        }
        for (Book book : booksToBorrow) {
            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException("Sách '" + book.getTitle() + "' đã hết, không thể mượn.");
            }
        }
        List<String> activeStatuses = List.of("BORROWED", "OVERDUE");
        // === RÀNG BUỘC MỚI: KIỂM TRA MƯỢN TRÙNG SÁCH ===
        for (Book book : booksToBorrow) {
            // Kiểm tra xem user có đang mượn cuốn sách này mà chưa trả không
            if (loanItemRepository.existsByBookAndBookLoan_UserAndStatusIn(book, user, activeStatuses)) {
                throw new IllegalStateException("Bạn đã mượn cuốn sách '" + book.getTitle() + "' và chưa trả.");
            }
            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException("Sách '" + book.getTitle() + "' đã hết, không thể mượn.");
            }
        }
        BookLoan newLoan = new BookLoan();
        newLoan.setUser(user);

        // =================================================================
        // <<< SỬA LỖI TẠI ĐÂY: Dùng LocalDate.now() thay vì LocalDateTime.now() >>>
        newLoan.setDueDate(LocalDate.now().plusDays(LOAN_DURATION_DAYS));
        // =================================================================

        newLoan.setStatus("ACTIVE");

        List<LoanItem> loanItems = new ArrayList<>();
        for (Book book : booksToBorrow) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            LoanItem item = new LoanItem();
            item.setBookLoan(newLoan);
            item.setBook(book);
            item.setStatus("BORROWED");
            loanItems.add(item);
        }
        newLoan.setLoanItems(loanItems);
        bookRepository.saveAll(booksToBorrow);
        bookLoanRepository.save(newLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanItemDto> getActiveLoansForUser(User user) {
        List<String> activeStatuses = List.of("BORROWED", "OVERDUE");
        List<LoanItem> loanItems = loanItemRepository.findByBookLoan_UserAndStatusIn(user, activeStatuses);
        return loanItems.stream()
                .map(LoanItemDto::new)
                .collect(Collectors.toList());
    }
    private final ReviewService reviewService;
    @Override
    @Transactional(readOnly = true)
    public Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable) {
        Page<LoanItem> loanItemPage = loanItemRepository.findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(user, pageable);

        // Dùng map của Page để chuyển đổi, đồng thời gán giá trị cho hasReviewed
        return loanItemPage.map(loanItem -> {
            LoanItemDto dto = new LoanItemDto(loanItem);
            // Kiểm tra xem user đã review sách này chưa
            boolean hasReviewed = reviewService.hasUserReviewedBook(user.getUserId(), loanItem.getBook().getId());
            dto.setHasReviewed(hasReviewed); // Gán kết quả vào DTO
            return dto;
        });
    }
}
