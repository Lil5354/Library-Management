package com.uef.library.service;

import com.uef.library.model.Book;
import com.uef.library.model.Category;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface BookService {

    Page<Book> listAllBooks(String keyword, Long categoryId, String availability,
                            Integer startYear, Integer endYear, String startsWith,
                            Pageable pageable);

    Page<Book> listAllBooksIn(String keyword, Long categoryId, String availability,
                            Integer startYear, Integer endYear, String startsWith,
                            Double minRating,
                            Pageable pageable);
    List<Category> getAllCategories();

    Optional<Book> getBookById(Long id);

    void saveBook(Book book);

    void deleteBookById(Long id);

    Optional<Category> getCategoryById(Long id);

    boolean isIsbnExists(String isbn);

    void importBooksFromExcel(MultipartFile file) throws IOException, IllegalArgumentException;

    void exportBooksToExcel(HttpServletResponse response) throws IOException;

    // <<< MERGE: THÊM TỪ ĐỒ ÁN 2 >>>
    List<Book> getBooksByIds(List<Long> bookIds);
}