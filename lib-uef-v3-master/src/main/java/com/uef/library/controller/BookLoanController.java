package com.uef.library.controller;

import com.uef.library.dto.BorrowRequestDTO;
import com.uef.library.dto.ReturnMultipleRequestDTO;
import com.uef.library.dto.ReturnMultipleResponseDTO;
import com.uef.library.dto.ReturnResponseDTO;
import com.uef.library.service.BookLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/book-loans")
@RequiredArgsConstructor
public class BookLoanController {

    private final BookLoanService bookLoanService;

    @GetMapping
    public ResponseEntity<?> getActiveBookLoans() {
        return ResponseEntity.ok(bookLoanService.getActiveBookLoans());
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBooks(@RequestBody BorrowRequestDTO borrowRequest) {
        try {
            return ResponseEntity.ok(bookLoanService.borrowBooks(borrowRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody Map<String, String> payload) {
        try {
            String userId = payload.get("userId");
            String isbn = payload.get("isbn");

            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng cung cấp ISBN của sách."));
            }
            ReturnResponseDTO response = bookLoanService.returnBook(userId, isbn);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // <<< THAY ĐỔI: Giữ nguyên endpoint nhưng logic bên trong service đã thay đổi >>>
    @PostMapping("/return-multiple")
    public ResponseEntity<?> returnMultipleBooks(@RequestBody ReturnMultipleRequestDTO returnRequest) {
        try {
            // Kiểm tra loanId thay vì userId
            if (returnRequest.getLoanId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã phiếu mượn (loanId) không được để trống."));
            }
            if (returnRequest.getIsbns() == null || returnRequest.getIsbns().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Danh sách ISBN không được để trống."));
            }
            ReturnMultipleResponseDTO response = bookLoanService.returnMultipleBooks(returnRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Đã có lỗi xảy ra ở máy chủ: " + e.getMessage()));
        }
    }

    @PostMapping("/renew/{loanId}")
    public ResponseEntity<?> renewBookLoan(@PathVariable Long loanId) {
        try {
            return ResponseEntity.ok(bookLoanService.renewBookLoan(loanId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}