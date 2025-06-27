package com.uef.library.controller;

import com.uef.library.model.Book;
import com.uef.library.model.User;
import com.uef.library.service.BookService;
import com.uef.library.service.BorrowServiceImpl;
import com.uef.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BorrowController {
    private final BorrowServiceImpl borrowService;
    private final UserService userService;
    private final BookService bookService;
    @PostMapping("/borrow/multiple")
    @ResponseBody // Trả về JSON
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<?> borrowMultipleBooks(@RequestBody List<Long> bookIds, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            List<Book> booksToBorrow = bookService.getBooksByIds(bookIds); // Giả sử có hàm này

            borrowService.createLoan(user, booksToBorrow);

            return ResponseEntity.ok(Map.of("message", "Bạn đã mượn thành công " + booksToBorrow.size() + " cuốn sách!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Đã có lỗi không mong muốn xảy ra."));
        }
    }
    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasRole('READER')")
    public String borrowBook(@PathVariable Long bookId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Book book = bookService.getBookById(bookId).orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

            borrowService.createLoan(user, List.of(book));
            redirectAttributes.addFlashAttribute("borrowSuccess", "Bạn đã mượn thành công cuốn sách: " + book.getTitle());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("borrowError", "Mượn sách thất bại: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("borrowError", "Đã có lỗi xảy ra, vui lòng thử lại.");
        }
        return "redirect:/categories";
    }
}