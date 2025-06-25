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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService { // <<< Triển khai từ BorrowService

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
        BookLoan newLoan = new BookLoan();
        newLoan.setUser(user);
        newLoan.setDueDate(LocalDateTime.now().plusDays(LOAN_DURATION_DAYS));
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
        List<LoanItem> loanItems = loanItemRepository.findByBookLoan_UserAndStatus(user, "BORROWED");
        return loanItems.stream()
                .map(LoanItemDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable) {
        Page<LoanItem> loanItemPage = loanItemRepository.findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(user, pageable);
        return loanItemPage.map(LoanItemDto::new); // Dùng map của Page để chuyển đổi
    }
}