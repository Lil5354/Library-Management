package com.uef.library.controller;

import com.uef.library.dto.ReaderDetailDTO;
import com.uef.library.model.Book;
import com.uef.library.model.Category;
import com.uef.library.model.User;
import com.uef.library.service.BookService;
import com.uef.library.service.ReaderManagementService;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;
    private final BookService bookService;
    private final ReaderManagementService readerManagementService;

    @GetMapping({"/home", "/dashboard"})
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String homePage(HttpSession session, Model model,
                           @RequestParam(name = "tab", required = false, defaultValue = "v-pills-dashboard") String activeTab,
                           @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                           @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                           // === CÁC THAM SỐ LỌC SÁCH ===
                           @RequestParam(name = "keyword", required = false) String keyword,
                           @RequestParam(name = "categoryId", required = false) Long categoryId,
                           @RequestParam(name = "availability", required = false) String availability,
                           @RequestParam(name = "startYear", required = false) Integer startYear,
                           @RequestParam(name = "endYear", required = false) Integer endYear,
                           @RequestParam(name = "startsWith", required = false) String startsWith,
                           // === CÁC THAM SỐ LỌC ĐỘC GIẢ ===
                           @RequestParam(name = "readerKeyword", required = false) String readerKeyword,
                           @RequestParam(name = "readerPage", required = false, defaultValue = "0") int readerPage,
                           @RequestParam(name = "readerSize", required = false, defaultValue = "10") int readerSize) {

        String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userService.findByUsername(username).orElse(null);
            if (user != null && user.getUserDetail() != null) {
                String fullName = user.getUserDetail().getFullName();
                if (fullName != null && !fullName.equalsIgnoreCase("Chưa cập nhật")) {
                    model.addAttribute("loggedInUserFullName", fullName);
                }
            }
        }

        // --- Xử lý cho tab Sách ---
        Pageable bookPageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookService.listAllBooks(keyword, categoryId, availability, startYear, endYear, startsWith, bookPageable);
        model.addAttribute("bookPage", bookPage);
        int totalBookPages = bookPage.getTotalPages();
        if (totalBookPages > 0) {
            List<Integer> bookPageNumbers = IntStream.rangeClosed(1, totalBookPages).boxed().collect(Collectors.toList());
            model.addAttribute("bookPageNumbers", bookPageNumbers);
        }
        model.addAttribute("categories", bookService.getAllCategories());
        if (!model.containsAttribute("newBook")) {
            model.addAttribute("newBook", new Book());
        }

        // --- Xử lý cho tab Độc giả ---
        Pageable readerPageable = PageRequest.of(readerPage, readerSize);
        Page<ReaderDetailDTO> readersPage = readerManagementService.getPaginatedReaders(readerKeyword, readerPageable);
        model.addAttribute("readersPage", readersPage);
        int totalReaderPages = readersPage.getTotalPages();
        if (totalReaderPages > 0) {
            List<Integer> readerPageNumbers = IntStream.rangeClosed(1, totalReaderPages).boxed().collect(Collectors.toList());
            model.addAttribute("readerPageNumbers", readerPageNumbers);
        }
        model.addAttribute("newReader", new ReaderDetailDTO());
        model.addAttribute("readerKeyword", readerKeyword);

        model.addAttribute("activeTab", activeTab);
        return "staff/index";
    }

    // ==========================================================
    // <<< CÁC ENDPOINT QUẢN LÝ SÁCH >>>
    // ==========================================================

    @PostMapping("/books/add")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String addBook(@ModelAttribute("newBook") Book book,
                          @RequestParam("categoryId") Long categoryId,
                          RedirectAttributes redirectAttributes) {

        if (bookService.isIsbnExists(book.getIsbn())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi! Mã ISBN '" + book.getIsbn() + "' đã tồn tại trong hệ thống.");
            redirectAttributes.addFlashAttribute("newBook", book);
            return "redirect:/staff/dashboard?tab=v-pills-sach";
        }

        Optional<Category> category = bookService.getCategoryById(categoryId);
        category.ifPresent(book::setCategory);
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sách thành công!");
        return "redirect:/staff/dashboard?tab=v-pills-sach";
    }

    @PostMapping("/books/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBook(@PathVariable("id") Long id) {
        try {
            bookService.deleteBookById(id);
            return ResponseEntity.ok("Xóa sách thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Không thể xóa sách này. Sách có thể đang được mượn hoặc có liên quan đến các dữ liệu khác.");
        }
    }

    @GetMapping("/books/export")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void exportBooks(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_danh_sach_sach.xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            bookService.exportBooksToExcel(response);
        } catch (IOException e) {
            System.err.println("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }

    @GetMapping("/books/api/{id}")
    @ResponseBody
    public ResponseEntity<Book> getBookById(@PathVariable("id") Long id) {
        Optional<Book> bookOptional = bookService.getBookById(id);
        return bookOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/books/update/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String updateBook(@PathVariable("id") Long id,
                             @ModelAttribute("bookDetails") Book bookDetails,
                             @RequestParam("categoryId") Long categoryId,
                             RedirectAttributes redirectAttributes) {
        Optional<Book> existingBookOpt = bookService.getBookById(id);
        if (existingBookOpt.isPresent()) {
            Book existingBook = existingBookOpt.get();
            existingBook.setTitle(bookDetails.getTitle());
            existingBook.setAuthor(bookDetails.getAuthor());
            existingBook.setPublisher(bookDetails.getPublisher());
            existingBook.setPublicationYear(bookDetails.getPublicationYear());
            existingBook.setAvailableCopies(bookDetails.getAvailableCopies());
            existingBook.setDescription(bookDetails.getDescription());
            existingBook.setCoverImage(bookDetails.getCoverImage());

            Optional<Category> category = bookService.getCategoryById(categoryId);
            category.ifPresent(existingBook::setCategory);

            bookService.saveBook(existingBook);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách để cập nhật.");
        }
        return "redirect:/staff/dashboard?tab=v-pills-sach";
    }

    @PostMapping("/books/import")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String importBooks(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("importError", "Vui lòng chọn một file Excel để tải lên.");
            return "redirect:/staff/dashboard?tab=v-pills-sach";
        }
        try {
            bookService.importBooksFromExcel(file);
            redirectAttributes.addFlashAttribute("successMessage", "Import danh sách sách thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("importError", "Đã xảy ra lỗi khi đọc file. Vui lòng kiểm tra định dạng file.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("importError", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("importError", "Đã có lỗi không mong muốn xảy ra trong quá trình import.");
            e.printStackTrace();
        }
        return "redirect:/staff/dashboard?tab=v-pills-sach";
    }

    // <<< MERGE: THÊM ENDPOINT UPLOAD PDF TỪ ĐỒ ÁN 2 >>>
    @PostMapping("/books/{id}/upload-sample")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadSamplePdf(@PathVariable("id") Long id, @RequestParam("sampleFile") MultipartFile file) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + id));

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn một file."));
        }

        try {
            Path uploadPath = Paths.get("uploads/samples");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = "sample_" + book.getId() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String pdfUrl = "/uploads/samples/" + uniqueFilename;
            book.setSamplePdfUrl(pdfUrl);
            bookService.saveBook(book);

            return ResponseEntity.ok(Map.of("message", "Tải lên file đọc thử thành công!", "filePath", pdfUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi lưu file."));
        }
    }

    // ==========================================================
    // <<< CÁC ENDPOINT QUẢN LÝ ĐỘC GIẢ >>>
    // ==========================================================

    @GetMapping("/readers/api/{userId}")
    @ResponseBody
    public ResponseEntity<ReaderDetailDTO> getReaderById(@PathVariable String userId) {
        return readerManagementService.findReaderById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/readers/add")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String addReader(@ModelAttribute("newReader") ReaderDetailDTO readerDto, RedirectAttributes redirectAttributes) {
        try {
            readerManagementService.createReader(readerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm độc giả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm độc giả: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newReader", readerDto);
        }
        return "redirect:/staff/dashboard?tab=v-pills-docgia";
    }

    @PostMapping("/readers/update/{userId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String updateReader(@PathVariable String userId, @ModelAttribute ReaderDetailDTO readerDto, RedirectAttributes redirectAttributes) {
        try {
            readerManagementService.updateReader(userId, readerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin độc giả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/staff/dashboard?tab=v-pills-docgia";
    }

    @GetMapping("/readers/delete/{userId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String deleteReader(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        try {
            readerManagementService.deleteReader(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa độc giả thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa độc giả này. Có thể do độc giả đang có sách mượn.");
        }
        return "redirect:/staff/dashboard?tab=v-pills-docgia";
    }

    @PostMapping("/readers/import")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String importReaders(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("importReaderError", "Vui lòng chọn một file để tải lên.");
            return "redirect:/staff/dashboard?tab=v-pills-docgia";
        }
        try {
            readerManagementService.importReadersFromExcel(file);
            redirectAttributes.addFlashAttribute("successMessage", "Import danh sách độc giả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("importReaderError", "Lỗi khi import file: " + e.getMessage());
        }
        return "redirect:/staff/dashboard?tab=v-pills-docgia";
    }

    @GetMapping("/readers/export")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void exportReaders(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Danh_sach_doc_gia.xlsx");
        try {
            readerManagementService.exportReadersToExcel(response);
        } catch (IOException e) {
            System.err.println("Lỗi khi xuất file Excel độc giả: " + e.getMessage());
        }
    }
}