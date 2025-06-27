package com.uef.library.service;

import com.uef.library.model.Book;
import com.uef.library.model.LoanItemDto;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BorrowService {
    void createLoan(User user, List<Book> booksToBorrow);

    List<LoanItemDto> getActiveLoansForUser(User user);

    Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable);

}