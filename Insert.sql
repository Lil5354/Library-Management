USE master;
GO

IF DB_ID('LibraryDB') IS NOT NULL
BEGIN
    ALTER DATABASE LibraryDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE LibraryDB;
    PRINT 'Database LibraryDB cũ đã được xóa.';
END
GO
CREATE DATABASE LibraryDB
COLLATE Latin1_General_100_CI_AI_SC_UTF8;
GO
PRINT 'Database LibraryDB đã được tạo thành công với Collation hỗ trợ UTF-8.';
-- Sử dụng cơ sở dữ liệu của bạn
USE LibraryDB;
GO

-- =================================================================
--    BƯỚC 2: TẠO LẠI CÁC BẢNG (Hibernate sẽ tự làm, nhưng chạy lại cho chắc)
--    Bạn có thể bỏ qua bước này nếu ddl-auto=update đang hoạt động tốt
-- =================================================================
-- Chạy lại ứng dụng Spring Boot của bạn để Hibernate tự tạo lại bảng.
-- Sau đó mới chạy tiếp Bước 3 và 4.

-- =================================================================
--    BƯỚC 3: INSERT DỮ LIỆU DANH MỤC VÀ SÁCH
-- =================================================================
PRINT '2. Inserting data into categories and books tables...';

INSERT INTO categories (name, description) VALUES
(N'Văn học kinh điển', N'Những tác phẩm văn học đã được khẳng định qua thời gian.'),
(N'Khoa học viễn tưởng', N'Các câu chuyện giả tưởng dựa trên các thành tựu khoa học kỹ thuật.'),
(N'Lịch sử', N'Sách ghi lại các sự kiện, nhân vật và giai đoạn lịch sử quan trọng.'),
(N'Kinh tế & Kinh doanh', N'Kiến thức về kinh tế học, quản trị, marketing và tài chính.'),
(N'Công nghệ thông tin', N'Sách chuyên ngành về lập trình, mạng máy tính, AI, và các công nghệ mới.'),
(N'Kỹ năng sống', N'Sách giúp phát triển bản thân, kỹ năng mềm và tư duy tích cực.'),
(N'Sách thiếu nhi', N'Truyện và sách kiến thức dành cho trẻ em.');

INSERT INTO books (isbn, title, author, publisher, publication_year, description, cover_image_url, category_id, total_copies, available_copies) VALUES
('9780743273565', N'Nhà Giả Kim', N'Paulo Coelho', N'NXB Văn học', 2011, N'Một câu chuyện đầy cảm hứng về việc theo đuổi ước mơ.', 'https://dimibook.com/wp-content/uploads/2024/03/nhung-bai-hoc-nhan-van-va-y-nghia-triet-ly-sau-sac-trong-cuon-sach-nha-gia-kim.png', (SELECT id FROM categories WHERE name = N'Văn học kinh điển'), 10, 8),
('9780061120084', N'Trăm năm cô đơn', N'Gabriel Garcia Marquez', N'NXB Hội Nhà Văn', 2003, N'Một trong những tác phẩm vĩ đại nhất của văn học thế kỷ 20.', 'https://images2.thanhnien.vn/528068263637045248/2024/12/23/19a1-ol-1734971366950706703256.jpg', (SELECT id FROM categories WHERE name = N'Văn học kinh điển'), 8, 5),
('9780307474278', N'Sapiens: Lược sử loài người', N'Yuval Noah Harari', N'NXB Tri Thức', 2014, N'Hành trình tiến hóa của loài người từ thời kỳ đồ đá đến nay.', 'https://bizweb.dktcdn.net/100/197/269/products/sapiens-luoc-su-ve-loai-nguoi-outline-5-7-2017-02.jpg?v=1520935327270', (SELECT id FROM categories WHERE name = N'Lịch sử'), 15, 15),
('9780132350884', N'Clean Code: A Handbook of Agile Software Craftsmanship', N'Robert C. Martin', N'Prentice Hall', 2008, N'Cuốn sách gối đầu giường cho mọi lập trình viên về cách viết mã sạch.', 'https://product.hstatic.net/200000239353/product/book_of_agile_software_craftsmanship__robert_c._martin___z-lib.org_-01_dbd3307686364767874117ca44b2d473_master.jpg', (SELECT id FROM categories WHERE name = N'Công nghệ thông tin'), 20, 18),
('9780553801477', N'Vũ trụ trong vỏ hạt dẻ', N'Stephen Hawking', N'NXB Trẻ', 2001, N'Khám phá những bí ẩn của vũ trụ qua lời giải thích của nhà vật lý thiên tài.', 'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1629465001i/19336635.jpg', (SELECT id FROM categories WHERE name = N'Khoa học viễn tưởng'), 7, 7),
('9780671027032', N'Đắc Nhân Tâm', N'Dale Carnegie', N'NXB Tổng hợp TP.HCM', 1936, N'Nghệ thuật giao tiếp và ứng xử để thành công trong cuộc sống.', 'https://bookfun.vn/wp-content/uploads/2024/07/dac-nhan-tam-sach.jpg.webp', (SELECT id FROM categories WHERE name = N'Kỹ năng sống'), 30, 25),
('9780316015844', N'Chạng vạng', N'Stephenie Meyer', N'NXB Trẻ', 2005, N'Câu chuyện tình lãng mạn giữa một cô gái và một ma cà rồng.', 'https://www.nxbtre.com.vn/Images/Book/nxbtre_full_04392020_023934.jpg', (SELECT id FROM categories WHERE name = N'Khoa học viễn tưởng'), 12, 0), -- Số lượng 0 để mô phỏng hết sách
('9780545010221', N'Harry Potter và Hòn đá Phù thủy', N'J.K. Rowling', N'NXB Trẻ', 1997, N'Cuộc phiêu lưu đầu tiên của cậu bé phù thủy Harry Potter tại trường Hogwarts.', 'https://www.nxbtre.com.vn/Images/Book/nxbtre_full_21042022_030444.jpg', (SELECT id FROM categories WHERE name = N'Sách thiếu nhi'), 25, 22),
('9781400033416', N'Tư duy nhanh và chậm', N'Daniel Kahneman', N'NXB Thế Giới', 2011, N'Khám phá hai hệ thống tư duy điều khiển cách chúng ta ra quyết định.', 'https://tiemsach.org/wp-content/uploads/2023/09/tu-e1693799006288.jpg', (SELECT id FROM categories WHERE name = N'Kỹ năng sống'), 10, 10),
('9781591847786', N'Từ tốt đến vĩ đại', N'Jim Collins', N'NXB Lao Động', 2001, N'Nghiên cứu về các yếu tố giúp một công ty nhảy vọt lên tầm vĩ đại.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTFAbMkPKAU01Uy6ApS0kdaudr-AScKn3yo6A&s', (SELECT id FROM categories WHERE name = N'Kinh tế & Kinh doanh'), 9, 8),
('9780134494166', N'Effective Java', N'Joshua Bloch', N'Addison-Wesley', 2017, N'Cung cấp các quy tắc và phương pháp hay nhất để lập trình Java hiệu quả.', 'https://m.media-amazon.com/images/I/71JAVv3TW4L.jpg', (SELECT id FROM categories WHERE name = N'Công nghệ thông tin'), 18, 18),
('9780439023528', N'Đấu trường sinh tử', N'Suzanne Collins', N'NXB Văn học', 2008, N'Trong một thế giới hậu tận thế, các thanh thiếu niên bị buộc phải tham gia vào một cuộc chiến sinh tồn trên truyền hình.', 'https://www.netabooks.vn/Data/Sites/1/Product/17326/1.jpg', (SELECT id FROM categories WHERE name = N'Khoa học viễn tưởng'), 11, 0), -- Số lượng 0 để mô phỏng hết sách
('9780767908184', N'Lược sử vạn vật', N'Bill Bryson', N'NXB Khoa học và Kỹ thuật', 2003, N'Một chuyến du hành hấp dẫn qua lịch sử của mọi thứ, từ Big Bang đến sự trỗi dậy của nền văn minh.', 'https://nhasachphuongnam.com/images/detailed/270/luoc-su-van-vat-tb-2023.jpg', (SELECT id FROM categories WHERE name = N'Lịch sử'), 6, 5),
('9781982134488', N'Nghĩ giàu và làm giàu', N'Napoleon Hill', N'NXB Tổng hợp TP.HCM', 1937, N'Một trong những cuốn sách self-help có ảnh hưởng nhất mọi thời đại, dựa trên nghiên cứu về những người thành công.', 'https://cdn1.fahasa.com/media/catalog/product/n/g/nghigiaulamgiau_110k-01_bia_1.jpg', (SELECT id FROM categories WHERE name = N'Kinh tế & Kinh doanh'), 15, 15),
('9780321765723', N'The Pragmatic Programmer: Your Journey to Mastery', N'Andrew Hunt, David Thomas', N'Addison-Wesley', 2019, N'Cung cấp các lời khuyên thực tế để cải thiện quy trình phát triển phần mềm và viết code linh hoạt hơn.', 'https://m.media-amazon.com/images/I/71f1jieYHNL._AC_UF1000,1000_QL80_.jpg', (SELECT id FROM categories WHERE name = N'Công nghệ thông tin'), 14, 12);
GO

PRINT '    -> Book data inserted successfully.';
GO

-- =================================================================
--    BƯỚC 4: INSERT DỮ LIỆU NGƯỜI DÙNG MỚI (ĐÃ SỬA ĐỔI)
-- =================================================================
PRINT '3. Inserting new user accounts...';

-- Mật khẩu là '123456' đã được mã hóa bằng BCrypt với hash $2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2
-- Thêm nhiều độc giả để phân bổ sách mượn
INSERT INTO users (user_id, username, password, role, status) VALUES
('ADMIN01', 'khoavo', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'ADMIN', 1),
('STAFF01', 'pupu26', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'STAFF', 1),
('READER01', 'nhanle', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1),
('READER02', 'maiphuong', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1),
('READER03', 'vanminh', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1),
('READER04', 'thanhhuyen', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1),
('READER05', 'duyanh', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1);
GO

INSERT INTO user_details (user_id, full_name, email, phone, address, dob, gender, membership_expiry_date) VALUES
('ADMIN01', N'Võ Đăng Khoa', 'khoavd222@uef.edu.vn', '0123456789', N'123 Đường ABC, Quận 1, TP.HCM', '2004-01-01', N'Nam', '2026-01-01'),
('STAFF01', N'Dương Thị Thanh Thảo', 'thaodtt22@uef.edu.vn', '0987654321', N'456 Đường XYZ, Quận 2, TP.HCM', '2004-02-02', N'Nữ', '2026-02-02'),
('READER01', N'Lê Thiện Nhân', 'nhanlt222@uef.edu.vn', '0555666777', N'789 Đường KLM, Quận 3, TP.HCM', '2004-03-03', N'Nam', '2026-03-03'),
('READER02', N'Nguyễn Mai Phương', 'phuongnm@uef.edu.vn', '0912345678', N'101 Đường Trần Hưng Đạo, Quận 1, TP.HCM', '2003-04-10', N'Nữ', '2026-04-10'),
('READER03', N'Trần Văn Minh', 'minhtv@uef.edu.vn', '0909876543', N'202 Đường Lê Lợi, Quận Gò Vấp, TP.HCM', '2001-07-20', N'Nam', '2026-07-20'),
('READER04', N'Phạm Thanh Huyền', 'huyenpt@uef.edu.vn', '0978123456', N'303 Đường Cách Mạng Tháng Tám, Quận 10, TP.HCM', '2000-11-05', N'Nữ', '2026-11-05'),
('READER05', N'Đỗ Duy Anh', 'duyanhd@uef.edu.vn', '0933221100', N'404 Đường Nguyễn Thị Minh Khai, Quận 3, TP.HCM', '2005-09-12', N'Nam', '2026-09-12');
GO

PRINT '    -> New user data inserted successfully.';
PRINT 'ALL SCRIPTS EXECUTED SUCCESSFULLY.';
GO

-- =================================================================
--    BƯỚC 5: INSERT SÁCH CÓ SAMPLE
-- =================================================================
USE LibraryDB;
GO

-- Xóa sách cũ nếu cần để tránh trùng lặp ISBN
DELETE FROM books WHERE isbn = 'UEF-JAVA-2024-01';
GO

PRINT 'Bắt đầu chèn dữ liệu sách "Thực hành công nghệ Java"...';

-- Giả sử sách này thuộc danh mục 'Công nghệ thông tin' (category_id = 5)
INSERT INTO books
    (title, author, publisher, publication_year, isbn, description, total_copies, available_copies, category_id, cover_image_url, sample_pdf_url)
VALUES
    (
        N'Thực hành công nghệ Java',
        N'Khoa Công nghệ thông tin - UEF',
        N'Đại học Kinh tế Tài chính TP.HCM (UEF)',
        2024,
        'UEF-JAVA-2024-01',
        N'Tài liệu hướng dẫn thực hành các bài lab về công nghệ Java, bao gồm Spring Framework, JPA, và các công nghệ liên quan.',
        50,
        50,
        5,
        '/uploads/covers/Java-tech.png',
        '/uploads/samples/Lab.pdf'
    );
GO

PRINT 'Hoàn tất chèn sách "Thực hành công nghệ Java".';
GO

-- =================================================================
--    BƯỚC 6: Dữ liệu lịch sử mượn sách (ĐÃ SỬA ĐỔI)
-- =================================================================
USE LibraryDB;
GO
PRINT 'Bắt đầu chèn dữ liệu lịch sử mượn sách...';

-- KỊCH BẢN 1: READER01 - Một phiếu mượn đã hoàn thành, trả sách ĐÚNG HẠN (2 cuốn)
-- READER01 mượn: 2 cuốn
DECLARE @loanId1 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER01', '2025-05-01 10:00:00', '2025-05-15', 'COMPLETED', 0);
SET @loanId1 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId1, (SELECT id FROM books WHERE isbn = '9780743273565'), 'RETURNED', '2025-05-10 14:00:00'), -- Nhà Giả Kim
    (@loanId1, (SELECT id FROM books WHERE isbn = '9780307474278'), 'RETURNED', '2025-05-14 09:00:00'); -- Sapiens
GO

-- KỊCH BẢN 2: READER01 - Một phiếu mượn đang active, VẪN CÒN HẠN (3 cuốn)
-- READER01 mượn: 2 + 3 = 5 cuốn (ĐẠT GIỚI HẠN)
DECLARE @loanId2 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER01', '2025-06-20 11:00:00', '2025-07-04', 'ACTIVE', 0); -- Vẫn còn hạn
SET @loanId2 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId2, (SELECT id FROM books WHERE isbn = '9781400033416'), 'BORROWED', NULL), -- Tư duy nhanh và chậm
    (@loanId2, (SELECT id FROM books WHERE isbn = '9781591847786'), 'BORROWED', NULL), -- Từ tốt đến vĩ đại
    (@loanId2, (SELECT id FROM books WHERE isbn = '9780134494166'), 'BORROWED', NULL); -- Effective Java
GO

-- KỊCH BẢN 3: READER02 - Một phiếu mượn đã hoàn thành, TRẢ TRỄ (2 cuốn - để tạo phí phạt)
-- READER02 mượn: 2 cuốn
DECLARE @loanId3 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER02', '2025-05-10 09:00:00', '2025-05-24', 'COMPLETED', 0);
SET @loanId3 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId3, (SELECT id FROM books WHERE isbn = '9780061120084'), 'RETURNED', '2025-05-28 16:00:00'), -- Trăm năm cô đơn (trễ 4 ngày)
    (@loanId3, (SELECT id FROM books WHERE isbn = '9780671027032'), 'RETURNED', '2025-05-30 10:00:00'); -- Đắc Nhân Tâm (trễ 6 ngày)
GO

-- KỊCH BẢN 4: READER02 - Một phiếu mượn đang active, QUÁ HẠN (1 cuốn - để tạo phí phạt hiện tại)
-- READER02 mượn: 2 + 1 = 3 cuốn
DECLARE @loanId4 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER02', '2025-06-01 14:00:00', '2025-06-15', 'ACTIVE', 0); -- Quá hạn (13 ngày tính đến 28/06)
SET @loanId4 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId4, (SELECT id FROM books WHERE isbn = '9780553801477'), 'BORROWED', NULL); -- Vũ trụ trong vỏ hạt dẻ
GO

-- KỊCH BẢN 5: READER03 - Một phiếu mượn đang active, QUÁ HẠN và ĐÃ GIA HẠN 1 LẦN
-- READER03 mượn: 2 cuốn
DECLARE @loanId5 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER03', '2025-05-15 10:00:00', '2025-05-29', 'ACTIVE', 0); -- Hạn đầu tiên
SET @loanId5 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId5, (SELECT id FROM books WHERE isbn = '9780545010221'), 'BORROWED', NULL), -- Harry Potter
    (@loanId5, (SELECT id FROM books WHERE isbn = '9780767908184'), 'BORROWED', NULL); -- Lược sử vạn vật

-- Gia hạn phiếu mượn này 1 lần (từ 29/05 -> 12/06) -> Bây giờ nó đã quá hạn tiếp (16 ngày tính đến 28/06)
UPDATE book_loans SET due_date = '2025-06-12', renewal_count = 1 WHERE id = @loanId5;
GO

-- KỊCH BẢN 6: READER04 - Một phiếu mượn đang active, VẪN CÒN HẠN (4 cuốn)
-- READER04 mượn: 4 cuốn
DECLARE @loanId6 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER04', '2025-06-25 09:00:00', '2025-07-09', 'ACTIVE', 0);
SET @loanId6 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId6, (SELECT id FROM books WHERE isbn = '9780132350884'), 'BORROWED', NULL), -- Clean Code
    (@loanId6, (SELECT id FROM books WHERE isbn = '9781982134488'), 'BORROWED', NULL), -- Nghĩ giàu và làm giàu
    (@loanId6, (SELECT id FROM books WHERE isbn = '9780321765723'), 'BORROWED', NULL), -- Pragmatic Programmer
    (@loanId6, (SELECT id FROM books WHERE isbn = 'UEF-JAVA-2024-01'), 'BORROWED', NULL); -- Thực hành công nghệ Java
GO

-- KỊCH BẢN 7: READER05 - Một phiếu mượn đã hoàn thành, TRẢ TRỄ (1 cuốn - để tạo thêm phí phạt)
-- READER05 mượn: 1 cuốn
DECLARE @loanId7 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER05', '2025-06-05 13:00:00', '2025-06-19', 'COMPLETED', 0);
SET @loanId7 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId7, (SELECT id FROM books WHERE isbn = '9780553801477'), 'RETURNED', '2025-06-25 10:00:00'); -- Vũ trụ trong vỏ hạt dẻ (trễ 6 ngày)
GO

PRINT 'Hoàn tất chèn dữ liệu lịch sử mượn sách.';

-- =================================================================
--    BƯỚC 7: TẠO DỮ LIỆU PHÍ PHẠT TỪ CÁC PHIẾU MƯỢN QUÁ HẠN (ĐÃ SỬA ĐỔI)
-- =================================================================
PRINT 'Bắt đầu tạo dữ liệu phí phạt...';
GO

-- Định nghĩa mức phí phạt mỗi ngày
DECLARE @LateFeePerDay FLOAT = 2000.0;
DECLARE @CurrentDate DATE = GETDATE(); -- Lấy ngày hiện tại

-- Chèn hoặc cập nhật các khoản phí cho những sách ĐÃ TRẢ nhưng BỊ TRỄ
INSERT INTO penalty_fees (loan_item_id, user_id, book_id, penalty_amount, overdue_days, status, created_at)
SELECT
    li.id,
    bl.user_id,
    li.book_id,
    DATEDIFF(day, bl.due_date, li.return_date) * @LateFeePerDay AS penalty_amount,
    DATEDIFF(day, bl.due_date, li.return_date) AS overdue_days,
    'UNPAID' AS status,
    GETDATE() AS created_at
FROM
    loan_items li
JOIN
    book_loans bl ON li.loan_id = bl.id
WHERE
    li.return_date IS NOT NULL
    AND CAST(li.return_date AS DATE) > bl.due_date -- Đảm bảo so sánh ngày
    AND NOT EXISTS (SELECT 1 FROM penalty_fees pf WHERE pf.loan_item_id = li.id);

-- Chèn hoặc cập nhật các khoản phí cho những sách CHƯA TRẢ và ĐÃ QUÁ HẠN (tính đến hôm nay)
INSERT INTO penalty_fees (loan_item_id, user_id, book_id, penalty_amount, overdue_days, status, created_at)
SELECT
    li.id,
    bl.user_id,
    li.book_id,
    DATEDIFF(day, bl.due_date, @CurrentDate) * @LateFeePerDay AS penalty_amount,
    DATEDIFF(day, bl.due_date, @CurrentDate) AS overdue_days,
    'UNPAID' AS status,
    GETDATE() AS created_at
FROM
    loan_items li
JOIN
    book_loans bl ON li.loan_id = bl.id
WHERE
    li.return_date IS NULL
    AND bl.due_date < @CurrentDate -- So sánh với ngày hiện tại (dạng DATE)
    AND NOT EXISTS (SELECT 1 FROM penalty_fees pf WHERE pf.loan_item_id = li.id);
GO

PRINT 'Hoàn tất tạo dữ liệu phí phạt.';
GO


USE LibraryDB;
GO
-- Đảm bảo tên người dùng là 'pupu26' nếu đó là tên bạn muốn làm STAFF/ADMIN
UPDATE users
SET role = 'STAFF' -- Đặt là STAFF để phù hợp với vai trò thủ thư
WHERE username = 'pupu26' AND role != 'ADMIN'; -- Chỉ cập nhật nếu không phải ADMIN
USE LibraryDB;
GO
UPDATE users
SET role = 'ADMIN' -- Nếu bạn muốn pupu26 là ADMIN, thì sửa lại dòng này
WHERE username = 'pu2610' AND role = 'STAFF'; -- Ví dụ, nếu pupu26 đã là STAFF, có thể nâng cấp lên ADMIN
GO

USE LibraryDB;

GO

UPDATE users

SET role = 'STAFF'

WHERE username = 'pu2610';

USE LibraryDB;

GO

-- =================================================================
--  BƯỚC 8: INSERT THÊM DỮ LIỆU MƯỢN SÁCH TỪ 20-29/06/2025
--  Mục đích: Làm cho biểu đồ thống kê theo ngày trông đẹp và liền mạch hơn.
-- =================================================================
USE LibraryDB;
GO
PRINT 'Bắt đầu chèn thêm dữ liệu mượn sách cho biểu đồ...';
GO

-- -----------------------------------------------------------------
-- Ngày 21/06/2025: Độc giả 05 bắt đầu mượn sách (2 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d21 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER05', '2025-06-21 14:00:00', DATEADD(day, 14, '2025-06-21'), 'ACTIVE', 0);
SET @loan_d21 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d21, (SELECT id FROM books WHERE isbn = '9781982134488'), 'BORROWED'), -- Nghĩ giàu và làm giàu
(@loan_d21, (SELECT id FROM books WHERE isbn = '9781400033416'), 'BORROWED'); -- Tư duy nhanh và chậm
GO

-- -----------------------------------------------------------------
-- Ngày 22/06/2025: Độc giả 02 mượn thêm (1 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d22 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER02', '2025-06-22 11:20:00', DATEADD(day, 14, '2025-06-22'), 'ACTIVE', 0);
SET @loan_d22 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d22, (SELECT id FROM books WHERE isbn = '9780132350884'), 'BORROWED'); -- Clean Code
GO

-- -----------------------------------------------------------------
-- Ngày 23/06/2025: Độc giả 03 mượn sách (2 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d23 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER03', '2025-06-23 09:15:00', DATEADD(day, 14, '2025-06-23'), 'ACTIVE', 0);
SET @loan_d23 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d23, (SELECT id FROM books WHERE isbn = '9780545010221'), 'BORROWED'), -- Harry Potter
(@loan_d23, (SELECT id FROM books WHERE isbn = '9780321765723'), 'BORROWED'); -- Pragmatic Programmer
GO

-- -----------------------------------------------------------------
-- Ngày 24/06/2025: Độc giả 05 mượn thêm (1 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d24 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER05', '2025-06-24 16:00:00', DATEADD(day, 14, '2025-06-24'), 'ACTIVE', 0);
SET @loan_d24 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d24, (SELECT id FROM books WHERE isbn = '9780307474278'), 'BORROWED'); -- Sapiens
GO

-- -----------------------------------------------------------------
-- Ngày 26/06/2025: Độc giả 01 mượn thêm (1 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d26 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER01', '2025-06-26 10:00:00', DATEADD(day, 14, '2025-06-26'), 'ACTIVE', 0);
SET @loan_d26 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d26, (SELECT id FROM books WHERE isbn = '9780134494166'), 'BORROWED'); -- Effective Java
GO

-- -----------------------------------------------------------------
-- Ngày 27/06/2025: Có 2 phiếu mượn khác nhau để tạo đỉnh cao hơn
-- -----------------------------------------------------------------
-- Phiếu 1: Độc giả 02 mượn 2 cuốn
DECLARE @loan_d27_1 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER02', '2025-06-27 13:30:00', DATEADD(day, 14, '2025-06-27'), 'ACTIVE', 0);
SET @loan_d27_1 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d27_1, (SELECT id FROM books WHERE isbn = '9780061120084'), 'BORROWED'), -- Trăm năm cô đơn
(@loan_d27_1, (SELECT id FROM books WHERE isbn = '9780743273565'), 'BORROWED'); -- Nhà Giả Kim
GO

-- Phiếu 2: Độc giả 04 mượn 1 cuốn
DECLARE @loan_d27_2 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER04', '2025-06-27 15:00:00', DATEADD(day, 14, '2025-06-27'), 'ACTIVE', 0);
SET @loan_d27_2 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d27_2, (SELECT id FROM books WHERE isbn = '9780767908184'), 'BORROWED'); -- Lược sử vạn vật
GO

-- -----------------------------------------------------------------
-- Ngày 28/06/2025: Độc giả 05 mượn thêm sách (2 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d28 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER05', '2025-06-28 09:00:00', DATEADD(day, 14, '2025-06-28'), 'ACTIVE', 0);
SET @loan_d28 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d28, (SELECT id FROM books WHERE isbn = '9780671027032'), 'BORROWED'), -- Đắc Nhân Tâm
(@loan_d28, (SELECT id FROM books WHERE isbn = '9780553801477'), 'BORROWED'); -- Vũ trụ trong vỏ hạt dẻ
GO

-- -----------------------------------------------------------------
-- Ngày 29/06/2025: Độc giả 03 mượn sách Java (1 cuốn)
-- -----------------------------------------------------------------
DECLARE @loan_d29 INT;
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('READER03', '2025-06-29 11:00:00', DATEADD(day, 14, '2025-06-29'), 'ACTIVE', 0);
SET @loan_d29 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status) VALUES
(@loan_d29, (SELECT id FROM books WHERE isbn = 'UEF-JAVA-2024-01'), 'BORROWED'); -- Thực hành công nghệ Java
GO

PRINT 'Hoàn tất chèn thêm dữ liệu mượn sách.';
GO