package com.uef.library.service;

import com.uef.library.dto.*;
import com.uef.library.model.*;
import com.uef.library.repository.BookLoanRepository;
import com.uef.library.repository.BookRepository;
import com.uef.library.repository.LoanItemRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final LoanItemRepository loanItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PenaltyFeeService penaltyFeeService;

    private static final int BORROW_LIMIT = 5;
    private static final int BORROW_DURATION_DAYS = 14;
    private static final double FINE_PER_DAY = 2000;
    private static final int RENEWAL_LIMIT = 1;


    @Override
    @Transactional(readOnly = true)
    public Page<BookLoanDTO> getActiveBookLoans(String search, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("dueDate").ascending());
        Page<BookLoan> bookLoansPage;
        if (search != null && !search.trim().isEmpty()) {
            bookLoansPage = bookLoanRepository.findActiveLoansBySearchTerm(search, sortedPageable);
        } else {
            bookLoansPage = bookLoanRepository.findAllActiveLoans(sortedPageable);
        }
        return bookLoansPage.map(this::convertToBookLoanDto);
    }

    @Override
    @Transactional
    public BookLoanDTO borrowBooks(BorrowRequestDTO request) throws Exception {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy độc giả với mã: " + request.getUserId()));

        List<Book> booksToBorrow = new ArrayList<>();
        for (String isbn : request.getBookIsbns()) {
            Book book = bookRepository.findByIsbn(isbn)
                    .orElseThrow(() -> new Exception("Không tìm thấy sách với ISBN: " + isbn));
            booksToBorrow.add(book);
        }

        BookLoan savedBookLoan = createLoanLogic(user, booksToBorrow);
        return convertToBookLoanDto(savedBookLoan);
    }

    @Override
    @Transactional
    public ReturnResponseDTO returnBook(String userId, String bookIsbn) throws Exception {
        LoanItem loanItem;
        if (userId != null && !userId.isBlank()) {
            // Trường hợp có độc giả: Tìm kiếm chính xác LoanItem của độc giả đó
            loanItem = loanItemRepository.findActiveLoanItemByUserIdAndBookIsbn(userId, bookIsbn)
                    .orElseThrow(() -> new Exception("Độc giả '" + userId + "' không có thông tin mượn sách với ISBN '" + bookIsbn + "' hoặc sách đã được trả."));
        } else {
            // Trường hợp không có độc giả (chỉ có ISBN):
            // Tìm TẤT CẢ LoanItem đang được mượn với ISBN này.
            List<LoanItem> activeLoanItems = loanItemRepository.findAllActiveLoanItemsByBookIsbn(bookIsbn);

            if (activeLoanItems.isEmpty()) {
                throw new Exception("Không tìm thấy sách nào đang được mượn với ISBN '" + bookIsbn + "'.");
            } else if (activeLoanItems.size() > 1) {
                // Nếu có nhiều hơn một LoanItem khớp, yêu cầu người dùng cung cấp mã độc giả
                String borrowedBy = activeLoanItems.stream()
                        .map(item -> item.getBookLoan().getUser().getUserDetail().getFullName() + " (" + item.getBookLoan().getUser().getUserId() + ")")
                        .collect(Collectors.joining(", "));
                throw new Exception("Sách với ISBN '" + bookIsbn + "' đang được mượn bởi nhiều độc giả. Vui lòng nhập Mã độc giả để xác định chính xác. (Đang được mượn bởi: " + borrowedBy + ")");
            } else {
                // Chỉ có một LoanItem khớp, tiến hành trả
                loanItem = activeLoanItems.get(0);
            }
        }
        return processReturn(loanItem);
    }

    @Override
    @Transactional
    public ReturnMultipleResponseDTO returnMultipleBooks(String userId, List<String> isbns) {
        List<LoanItemDTO> successfulReturns = new ArrayList<>();
        List<PenaltyFeeDTO> generatedPenalties = new ArrayList<>();
        List<String> processingErrors = new ArrayList<>();

        for (String isbn : isbns) {
            try {
                LoanItem itemToReturn;
                if (userId != null && !userId.isBlank()) {
                    // Trường hợp có độc giả: Tìm kiếm chính xác LoanItem của độc giả đó
                    itemToReturn = loanItemRepository.findActiveLoanItemByUserIdAndBookIsbn(userId, isbn)
                            .orElseThrow(() -> new Exception("Độc giả '" + userId + "' không có sách đang mượn với ISBN '" + isbn + "' hoặc sách đã được trả."));
                } else {
                    // Trường hợp không có độc giả (chỉ có ISBN):
                    // Tìm TẤT CẢ LoanItem đang được mượn với ISBN này.
                    List<LoanItem> activeLoanItems = loanItemRepository.findAllActiveLoanItemsByBookIsbn(isbn);

                    if (activeLoanItems.isEmpty()) {
                        throw new Exception("Không tìm thấy sách nào đang được mượn với ISBN '" + isbn + "'.");
                    } else if (activeLoanItems.size() > 1) {
                        // Nếu có nhiều hơn một LoanItem khớp, đây là trường hợp cần xử lý.
                        // Trong ngữ cảnh "trả nhiều sách", tốt nhất là báo lỗi và yêu cầu nhập userId.
                        // Hoặc, nếu có một quy tắc rõ ràng (ví dụ, luôn trả cuốn được mượn sớm nhất nếu không có userId),
                        // thì có thể chọn `activeLoanItems.get(0)`.
                        // Với mục đích sửa lỗi "unique result" và yêu cầu nhập thêm thông tin,
                        // chúng ta sẽ báo lỗi.
                        String borrowedBy = activeLoanItems.stream()
                                .map(item -> item.getBookLoan().getUser().getUserDetail().getFullName() + " (" + item.getBookLoan().getUser().getUserId() + ")")
                                .collect(Collectors.joining(", "));
                        throw new Exception("Sách với ISBN '" + isbn + "' đang được mượn bởi nhiều độc giả. Vui lòng nhập Mã độc giả để xác định chính xác. (Đang được mượn bởi: " + borrowedBy + ")");
                    } else {
                        // Chỉ có một LoanItem khớp, tiến hành trả
                        itemToReturn = activeLoanItems.get(0);
                    }
                }

                ReturnResponseDTO singleReturnResponse = processReturn(itemToReturn);
                if (singleReturnResponse.getReturnedItem() != null) {
                    successfulReturns.add(singleReturnResponse.getReturnedItem());
                }
                if (singleReturnResponse.getPenaltyFee() != null) {
                    generatedPenalties.add(singleReturnResponse.getPenaltyFee());
                }
                checkAndCompleteLoan(itemToReturn.getBookLoan());

            } catch (Exception e) {
                processingErrors.add("Sách ISBN '" + isbn + "': " + e.getMessage());
            }
        }
        return new ReturnMultipleResponseDTO(successfulReturns, generatedPenalties, processingErrors);
    }

    @Override
    @Transactional
    public BookLoanDTO renewBookLoan(Long bookLoanId) throws Exception {
        BookLoan bookLoan = bookLoanRepository.findById(bookLoanId)
                .orElseThrow(() -> new Exception("Không tìm thấy phiếu mượn với ID: " + bookLoanId));

        if ("COMPLETED".equals(bookLoan.getStatus())) {
            throw new Exception("Phiếu mượn đã hoàn tất, không thể gia hạn.");
        }
        if (bookLoan.getRenewalCount() >= RENEWAL_LIMIT) {
            throw new Exception("Phiếu mượn này đã được gia hạn tối đa " + RENEWAL_LIMIT + " lần.");
        }
        if (LocalDate.now().isAfter(bookLoan.getDueDate())) {
            throw new Exception("Phiếu mượn đã quá hạn, không thể gia hạn. Vui lòng trả sách và nộp phạt (nếu có).");
        }
        for (LoanItem item : bookLoan.getLoanItems()) {
            if ("BORROWED".equals(item.getStatus()) && penaltyFeeService.hasUnpaidPenalty(item.getId())) {
                throw new Exception("Không thể gia hạn. Sách '"+ item.getBook().getTitle() +"' trong phiếu mượn có phí phạt chưa thanh toán.");
            }
        }
        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(BORROW_DURATION_DAYS));
        bookLoan.setRenewalCount(bookLoan.getRenewalCount() + 1);
        BookLoan renewedLoan = bookLoanRepository.save(bookLoan);
        return convertToBookLoanDto(renewedLoan);
    }

    @Override
    @Transactional
    public void createLoanForReader(User user, List<Book> booksToBorrow) throws Exception {
        validateUser(user);
        createLoanLogic(user, booksToBorrow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanItemDto> getActiveLoansForUser(User user) {
        List<String> activeStatuses = List.of("BORROWED");
        List<LoanItem> loanItems = loanItemRepository.findByBookLoan_UserAndStatus(user, "BORROWED");
        return loanItems.stream()
                .map(LoanItemDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanItemDto> getLoanHistoryForUser(User user, Pageable pageable) {
        // Đảm bảo import org.springframework.data.domain.Page và Pageable đã có
        Page<LoanItem> loanItemPage = loanItemRepository.findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(user, pageable);
        return loanItemPage.map(LoanItemDto::new);
    }

    private ReturnResponseDTO processReturn(LoanItem loanItem) throws Exception {
        loanItem.setReturnDate(LocalDateTime.now());
        loanItem.setStatus("RETURNED");
        LoanItem updatedLoanItem = loanItemRepository.save(loanItem);

        Book book = updatedLoanItem.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        PenaltyFeeDTO newPenaltyFee = null;
        BookLoan bookLoan = updatedLoanItem.getBookLoan();
        LocalDate returnDate = updatedLoanItem.getReturnDate().toLocalDate();

        if (returnDate.isAfter(bookLoan.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(bookLoan.getDueDate(), returnDate);
            if (overdueDays > 0) {
                double fineAmount = overdueDays * FINE_PER_DAY;
                if (!penaltyFeeService.hasUnpaidPenalty(updatedLoanItem.getId())) {
                    newPenaltyFee = penaltyFeeService.createPenaltyFeeForOverdueBorrowing(
                            updatedLoanItem.getId(), (int) overdueDays, fineAmount);
                }
            }
        }
        return new ReturnResponseDTO(convertToSimpleLoanItemDto(updatedLoanItem), newPenaltyFee);
    }

    private BookLoan createLoanLogic(User user, List<Book> booksToBorrow) throws Exception {
        validateUser(user);
        if (booksToBorrow == null || booksToBorrow.isEmpty()) {
            throw new IllegalStateException("Vui lòng chọn ít nhất một cuốn sách để mượn.");
        }
        long currentBorrowedCount = loanItemRepository.countActiveLoansByUser(user);
        if (currentBorrowedCount + booksToBorrow.size() > BORROW_LIMIT) {
            throw new IllegalStateException("Bạn chỉ có thể mượn tối đa " + BORROW_LIMIT + " cuốn sách. Bạn đang mượn " + currentBorrowedCount + " cuốn.");
        }
        for (Book book : booksToBorrow) {
            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException("Sách '" + book.getTitle() + "' đã hết, không thể mượn.");
            }
        }
        BookLoan newLoan = new BookLoan();
        newLoan.setUser(user);
        newLoan.setDueDate(LocalDate.now().plusDays(BORROW_DURATION_DAYS));
        newLoan.setStatus("ACTIVE");

        for (Book book : booksToBorrow) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            LoanItem item = new LoanItem();
            item.setBook(book);
            item.setStatus("BORROWED");
            newLoan.addLoanItem(item);
        }
        bookRepository.saveAll(booksToBorrow);
        return bookLoanRepository.save(newLoan);
    }

    private void validateUser(User user) throws Exception {
        if (!user.isStatus()) {
            throw new Exception("Tài khoản độc giả này đang bị khóa.");
        }
        if (user.getUserDetail() != null && user.getUserDetail().getMembershipExpiryDate() != null &&
                user.getUserDetail().getMembershipExpiryDate().isBefore(LocalDate.now())) {
            throw new Exception("Thẻ thành viên của độc giả đã hết hạn.");
        }
    }

    private void checkAndCompleteLoan(BookLoan bookLoan) {
        List<LoanItem> items = loanItemRepository.findByBookLoanId(bookLoan.getId());
        boolean allReturned = items.stream().allMatch(item -> "RETURNED".equals(item.getStatus()));
        if (allReturned) {
            bookLoan.setStatus("COMPLETED");
            bookLoanRepository.save(bookLoan);
        }
    }

    private BookLoanDTO convertToBookLoanDto(BookLoan loan) {
        List<LoanItemDTO> itemDTOs = loan.getLoanItems().stream()
                .map(this::convertToSimpleLoanItemDto)
                .collect(Collectors.toList());

        long overdueDays = 0;
        if ("ACTIVE".equals(loan.getStatus()) && LocalDate.now().isAfter(loan.getDueDate())) {
            overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        }

        return BookLoanDTO.builder()
                .loanId(loan.getId())
                .readerName(loan.getUser().getUserDetail().getFullName())
                .readerId(loan.getUser().getUserId())
                .borrowDate(loan.getBorrowDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .renewalCount(loan.getRenewalCount())
                .items(itemDTOs)
                .overdueDays(overdueDays)
                .fineAmount(overdueDays * FINE_PER_DAY)
                .build();
    }

    private LoanItemDTO convertToSimpleLoanItemDto(LoanItem item) {
        return LoanItemDTO.builder()
                .itemId(item.getId())
                .bookTitle(item.getBook().getTitle())
                .bookIsbn(item.getBook().getIsbn())
                .status(item.getStatus())
                .returnDate(item.getReturnDate())
                .build();
    }
}