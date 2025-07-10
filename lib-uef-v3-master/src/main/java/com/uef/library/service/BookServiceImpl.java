package com.uef.library.service;

import com.uef.library.config.BookSpecification;
import com.uef.library.model.Book;
import com.uef.library.model.Category;
import com.uef.library.repository.BookRepository;
import com.uef.library.repository.CategoryRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Page<Book> listAllBooks(String keyword, Long categoryId, String availability,
                                   Integer startYear, Integer endYear, String startsWith, Pageable pageable) {
        // Cập nhật lời gọi đến Specification
        Specification<Book> spec = BookSpecification.filterBy(keyword, categoryId, availability, startYear, endYear, startsWith);
        return bookRepository.findAll(spec, pageable);
    }
    @Override
    public Page<Book> listAllBooksIn(String keyword, Long categoryId, String availability,
                                     Integer startYear, Integer endYear, String startsWith,
                                     Double minRating, // <<< THÊM VÀO ĐÂY
                                     Pageable pageable) {
        // Truyền tham số mới vào Specification
        Specification<Book> spec = BookSpecification.filterByIn(keyword, categoryId, availability, startYear, endYear, startsWith, minRating);
        return bookRepository.findAll(spec, pageable);
    }
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public boolean isIsbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional
    public void importBooksFromExcel(MultipartFile file) throws IOException, IllegalArgumentException {
        List<Book> booksToSave = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Bỏ qua dòng tiêu đề
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Lấy dữ liệu từ các cột
                String isbn = getCellValue(row.getCell(0));
                String title = getCellValue(row.getCell(1));
                String author = getCellValue(row.getCell(2));
                String categoryName = getCellValue(row.getCell(3));
                String publisher = getCellValue(row.getCell(4));
                String pubYearStr = getCellValue(row.getCell(5));
                String quantityStr = getCellValue(row.getCell(6));

                // THÊM: Đọc cột mô tả sách (chỉ mục 7) và link ảnh (chỉ mục 8)
                String description = getCellValue(row.getCell(7));
                String coverImage = getCellValue(row.getCell(8));


                if (isbn.isEmpty() || title.isEmpty() || categoryName.isEmpty() || quantityStr.isEmpty()) {
                    System.err.println("Bỏ qua hàng thiếu thông tin bắt buộc: ISBN, Tiêu đề, Thể loại, Số lượng. Hàng: " + row.getRowNum());
                    continue;
                }
                if (bookRepository.existsByIsbn(isbn)) {
                    System.out.println("Bỏ qua sách có ISBN đã tồn tại: " + isbn);
                    continue;
                }

                Category category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(categoryName);
                            // Lưu category mới để tránh lỗi TransientObjectException
                            return categoryRepository.save(newCategory);
                        });

                Book book = new Book();
                book.setIsbn(isbn);
                book.setTitle(title);
                book.setAuthor(author);
                book.setPublisher(publisher);
                book.setCategory(category);

                // THÊM: Gán giá trị cho mô tả và link ảnh
                book.setDescription(description.isEmpty() ? null : description);
                book.setCoverImage(coverImage.isEmpty() ? null : coverImage);

                try {
                    int publicationYear = Integer.parseInt(pubYearStr);
                    book.setPublicationYear(publicationYear);

                    int quantity = Integer.parseInt(quantityStr);
                    book.setTotalCopies(quantity);
                    book.setAvailableCopies(quantity);
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi định dạng số (Năm XB hoặc Số lượng) cho sách có ISBN: " + isbn + ". Bỏ qua hàng.");
                    continue;
                }
                booksToSave.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để dễ debug
            throw new IOException("Định dạng file Excel không hợp lệ hoặc file bị lỗi: " + e.getMessage(), e);
        }

        if (booksToSave.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu sách hợp lệ nào trong file Excel.");
        }
        bookRepository.saveAll(booksToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportBooksToExcel(HttpServletResponse response) throws IOException {
        List<Book> books = bookRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Danh sách Sách");

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // CẬP NHẬT: Thêm cột "Mô tả sách" và "Link ảnh bìa" vào header
            String[] headers = {"ISBN", "Tên sách", "Tác giả", "Thể loại", "Nhà xuất bản", "Năm XB", "Tổng số", "Sẵn có", "Mô tả sách", "Link ảnh bìa"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(book.getIsbn());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getAuthor());
                row.createCell(3).setCellValue(book.getCategory() != null ? book.getCategory().getName() : "N/A");
                row.createCell(4).setCellValue(book.getPublisher());
                row.createCell(5).setCellValue(book.getPublicationYear() != null ? book.getPublicationYear().toString() : "");
                row.createCell(6).setCellValue(book.getTotalCopies());
                row.createCell(7).setCellValue(book.getAvailableCopies());
                // THÊM: Ghi giá trị cho mô tả và link ảnh
                row.createCell(8).setCellValue(book.getDescription() != null ? book.getDescription() : "");
                row.createCell(9).setCellValue(book.getCoverImage() != null ? book.getCoverImage() : "");
            }

            // Điều chỉnh độ rộng cột tự động cho tất cả các cột mới
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Đặt tên file và header cho response
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=books_report_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader(headerKey, headerValue);

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace(); // In lỗi để dễ debug
            throw new IOException("Lỗi khi tạo và ghi file Excel: " + e.getMessage(), e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Xử lý cả số nguyên và số thập phân, trả về dạng chuỗi
                DataFormatter dataFormatter = new DataFormatter();
                return dataFormatter.formatCellValue(cell).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Đánh giá công thức và trả về giá trị chuỗi
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                if (cellValue.getCellType() == CellType.NUMERIC) {
                    return String.valueOf((long) cellValue.getNumberValue());
                } else {
                    return cellValue.getStringValue().trim();
                }
            default:
                return "";
        }
    }
    @Override
    public List<Book> getBooksByIds(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return List.of(); // Trả về danh sách rỗng nếu không có ID nào
        }
        return bookRepository.findAllByIdIn(bookIds);
    }
}