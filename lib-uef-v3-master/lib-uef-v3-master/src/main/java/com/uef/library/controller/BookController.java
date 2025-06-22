package com.uef.library.controller;

import com.uef.library.model.Book;
import com.uef.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/details/{id}")
    public String bookDetails(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOptional = bookService.getBookById(id);
        if (bookOptional.isPresent()) {
            model.addAttribute("book", bookOptional.get());
            // Có thể thêm các sách liên quan ở đây nếu muốn
            // model.addAttribute("relatedBooks", ...);
            return "home/book-detail"; // Trỏ đến file view mới
        }
        // Nếu không tìm thấy sách, trả về trang lỗi 404
        return "error/404";
    }
}