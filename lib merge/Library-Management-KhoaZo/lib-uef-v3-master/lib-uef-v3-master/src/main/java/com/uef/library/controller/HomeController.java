package com.uef.library.controller;

import com.uef.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;
    @GetMapping("/")
    public String homePage() {
        return "home/index";
    }

    // === CẬP NHẬT PHƯƠNG THỨC NÀY ===
    @GetMapping("/categories")
    public String viewCategories(Model model,
                                 @RequestParam(name = "keyword", required = false) String keyword,
                                 @RequestParam(name = "categoryId", required = false) Long categoryId,
                                 @RequestParam(name = "availability", required = false) String availability,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "9") int size,
                                 @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Xử lý thông tin sắp xếp
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Gọi service để lấy dữ liệu
// Truyền null cho các tham số lọc nâng cao không dùng đến ở trang này
        model.addAttribute("bookPage", bookService.listAllBooks(keyword, categoryId, availability, null, null, null, pageable));
        model.addAttribute("categories", bookService.getAllCategories());

        // Gửi các tham số lọc lại cho view để giữ trạng thái
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("availability", availability); // Thêm dòng này
        model.addAttribute("sort", sort);

        return "home/categories";
    }

    @GetMapping("/about")
    public String about() {
        return "home/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "home/contact";
    }
}