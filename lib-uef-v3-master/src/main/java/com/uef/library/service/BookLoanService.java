package com.uef.library.service;

import com.uef.library.dto.*;
import com.uef.library.model.Book;
import com.uef.library.model.LoanItemDto;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * <<< FILE HỢP NHẤT >>>
 * Service duy nhất quản lý tất cả các nghiệp vụ liên quan đến mượn và trả sách.
 * Bao gồm cả chức năng cho Thủ thư (từ BookLoanService cũ) và Độc giả (từ BorrowService cũ).
 */
public interface BookLoanService {

    // ==========================================================
    // <<< NGHIỆP VỤ CHO THỦ THƯ (TỪ ĐỒ ÁN 1) >>>
    // ==========================================================

    List<BookLoanDTO> getActiveBookLoans();
    BookLoanDTO borrowBooks(BorrowRequestDTO request) throws Exception;
    ReturnResponseDTO returnBook(String userId, String bookIsbn) throws Exception;
    BookLoanDTO renewBookLoan(Long bookLoanId) throws Exception;
    ReturnMultipleResponseDTO returnMultipleBooks(ReturnMultipleRequestDTO request);

    // ==========================================================
    // <<< NGHIỆP VỤ CHO ĐỘC GIẢ (TỪ ĐỒ ÁN 2) >>>
    // ==========================================================

    void createLoanForReader(User user, List<Book> booksToBorrow) throws Exception;
    List<LoanItemDto> getActiveLoansForUser(User user);
    Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable);
}
