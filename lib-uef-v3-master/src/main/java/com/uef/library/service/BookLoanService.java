package com.uef.library.service;

import com.uef.library.dto.BookLoanDTO;
import com.uef.library.dto.BorrowRequestDTO;
import com.uef.library.model.LoanItemDto;
import com.uef.library.dto.ReturnMultipleResponseDTO;
import com.uef.library.dto.ReturnResponseDTO;
import com.uef.library.model.Book;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookLoanService {

    Page<BookLoanDTO> getActiveBookLoans(String search, Pageable pageable);

    BookLoanDTO borrowBooks(BorrowRequestDTO request) throws Exception;

    ReturnResponseDTO returnBook(String userId, String bookIsbn) throws Exception;

    ReturnMultipleResponseDTO returnMultipleBooks(String userId, List<String> isbns);

    BookLoanDTO renewBookLoan(Long bookLoanId) throws Exception;

    void createLoanForReader(User user, List<Book> booksToBorrow) throws Exception;

    List<LoanItemDto> getActiveLoansForUser(User user);

    Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable);
}