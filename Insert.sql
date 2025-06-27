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
('9780316015844', N'Chạng vạng', N'Stephenie Meyer', N'NXB Trẻ', 2005, N'Câu chuyện tình lãng mạn giữa một cô gái và một ma cà rồng.', 'https://www.nxbtre.com.vn/Images/Book/nxbtre_full_04392020_023934.jpg', (SELECT id FROM categories WHERE name = N'Khoa học viễn tưởng'), 12, 0),
('9780545010221', N'Harry Potter và Hòn đá Phù thủy', N'J.K. Rowling', N'NXB Trẻ', 1997, N'Cuộc phiêu lưu đầu tiên của cậu bé phù thủy Harry Potter tại trường Hogwarts.', 'https://www.nxbtre.com.vn/Images/Book/nxbtre_full_21042022_030444.jpg', (SELECT id FROM categories WHERE name = N'Sách thiếu nhi'), 25, 22),
('9781400033416', N'Tư duy nhanh và chậm', N'Daniel Kahneman', N'NXB Thế Giới', 2011, N'Khám phá hai hệ thống tư duy điều khiển cách chúng ta ra quyết định.', 'https://tiemsach.org/wp-content/uploads/2023/09/tu-e1693799006288.jpg', (SELECT id FROM categories WHERE name = N'Kỹ năng sống'), 10, 10),
('9781591847786', N'Từ tốt đến vĩ đại', N'Jim Collins', N'NXB Lao Động', 2001, N'Nghiên cứu về các yếu tố giúp một công ty nhảy vọt lên tầm vĩ đại.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTFAbMkPKAU01Uy6ApS0kdaudr-AScKn3yo6A&s', (SELECT id FROM categories WHERE name = N'Kinh tế & Kinh doanh'), 9, 8),
('9780134494166', N'Effective Java', N'Joshua Bloch', N'Addison-Wesley', 2017, N'Cung cấp các quy tắc và phương pháp hay nhất để lập trình Java hiệu quả.', 'https://m.media-amazon.com/images/I/71JAVv3TW4L.jpg', (SELECT id FROM categories WHERE name = N'Công nghệ thông tin'), 18, 18),
('9780439023528', N'Đấu trường sinh tử', N'Suzanne Collins', N'NXB Văn học', 2008, N'Trong một thế giới hậu tận thế, các thanh thiếu niên bị buộc phải tham gia vào một cuộc chiến sinh tồn trên truyền hình.', 'https://www.netabooks.vn/Data/Sites/1/Product/17326/1.jpg', (SELECT id FROM categories WHERE name = N'Khoa học viễn tưởng'), 11, 0),
('9780767908184', N'Lược sử vạn vật', N'Bill Bryson', N'NXB Khoa học và Kỹ thuật', 2003, N'Một chuyến du hành hấp dẫn qua lịch sử của mọi thứ, từ Big Bang đến sự trỗi dậy của nền văn minh.', 'https://nhasachphuongnam.com/images/detailed/270/luoc-su-van-vat-tb-2023.jpg', (SELECT id FROM categories WHERE name = N'Lịch sử'), 6, 5),
('9781982134488', N'Nghĩ giàu và làm giàu', N'Napoleon Hill', N'NXB Tổng hợp TP.HCM', 1937, N'Một trong những cuốn sách self-help có ảnh hưởng nhất mọi thời đại, dựa trên nghiên cứu về những người thành công.', 'https://cdn1.fahasa.com/media/catalog/product/n/g/nghigiaulamgiau_110k-01_bia_1.jpg', (SELECT id FROM categories WHERE name = N'Kinh tế & Kinh doanh'), 15, 15),
('9780321765723', N'The Pragmatic Programmer: Your Journey to Mastery', N'Andrew Hunt, David Thomas', N'Addison-Wesley', 2019, N'Cung cấp các lời khuyên thực tế để cải thiện quy trình phát triển phần mềm và viết code linh hoạt hơn.', 'https://m.media-amazon.com/images/I/71f1jieYHNL._AC_UF1000,1000_QL80_.jpg', (SELECT id FROM categories WHERE name = N'Công nghệ thông tin'), 14, 12);
GO

PRINT '   -> Book data inserted successfully.';
GO

-- =================================================================
--    BƯỚC 4: INSERT DỮ LIỆU NGƯỜI DÙNG MỚI
-- =================================================================
PRINT '3. Inserting new user accounts...';

-- Mật khẩu là '123456' đã được mã hóa bằng BCrypt với hash $2a$10$IicsOii2/pUOYYWGGON5kOEy3j3z.W6fJjbShalwbCaXtIgNIYvq2
INSERT INTO users (user_id, username, password, role, status) VALUES
('ADMIN01', 'khoavo', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'ADMIN', 1),
('STAFF01', 'pupu', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'STAFF', 1),
('READER01', 'nhanle', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1);
GO

INSERT INTO user_details (user_id, full_name, email, phone, address, dob, gender) VALUES
('ADMIN01', N'Võ Đăng Khoa', 'khoavd222@uef.edu.vn', '0123456789', N'123 Đường ABC, Quận 1, TP.HCM', '2004-01-01', N'Nam'),
('STAFF01', N'Dương Thị Thanh Thảo', 'thaodtt22@uef.edu.vn', '0987654321', N'456 Đường XYZ, Quận 2, TP.HCM', '2004-02-02', N'Nữ'),
('READER01', N'Lê Thiện Nhân', 'nhanlt222@uef.edu.vn', '0555666777', N'789 Đường KLM, Quận 3, TP.HCM', '2004-03-03', N'Nam');
GO

PRINT '   -> New user data inserted successfully.';
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
        N'Thực hành công nghệ Java',                        -- title
        N'Khoa Công nghệ thông tin - UEF',                  -- author
        N'Đại học Kinh tế Tài chính TP.HCM (UEF)',          -- publisher
        2024,                                              -- publication_year
        'UEF-JAVA-2024-01',                                -- isbn
        N'Tài liệu hướng dẫn thực hành các bài lab về công nghệ Java, bao gồm Spring Framework, JPA, và các công nghệ liên quan.', -- description
        50,                                                -- total_copies
        50,                                                -- available_copies
        5,                                                 -- category_id (ID của danh mục CNTT)
        '/uploads/covers/Java-tech.png',                   -- Sửa lại thành .png
        '/uploads/samples/Lab.pdf'                         -- sample_pdf_url
    );
GO

PRINT 'Hoàn tất chèn sách "Thực hành công nghệ Java".';
GO

-- =================================================================
--    BƯỚC 6: Dữ liệu lịch sử mượn sách
-- =================================================================
-- =================================================================
--   BƯỚC 6: Dữ liệu lịch sử mượn sách (ĐÃ SỬA LỖI)
-- =================================================================
USE LibraryDB;
GO
PRINT 'Chuẩn bị thêm tài khoản READER R0001...';

-- Xóa người dùng cũ có ID R0001 để tránh lỗi trùng lặp
IF EXISTS (SELECT 1 FROM users WHERE user_id = 'R0001')
BEGIN
    -- Phải xóa ở các bảng con trước
    -- Chú ý: Cần xóa các bản ghi liên quan trong penalty_fees, loan_items, book_loans trước
    DELETE pf FROM penalty_fees pf JOIN loan_items li ON pf.loan_item_id = li.id JOIN book_loans bl ON li.loan_id = bl.id WHERE bl.user_id = 'R0001';
    DELETE li FROM loan_items li JOIN book_loans bl ON li.loan_id = bl.id WHERE bl.user_id = 'R0001';
    DELETE FROM book_loans WHERE user_id = 'R0001';
    DELETE FROM notifications WHERE recipient_user_id = 'R0001';
    DELETE FROM user_details WHERE user_id = 'R0001';
    
    -- Xóa ở bảng cha sau
    DELETE FROM users WHERE user_id = 'R0001';
    PRINT ' -> Đã xóa tài khoản R0001 cũ và các dữ liệu liên quan.';
END
GO

-- Thêm tài khoản mới
PRINT 'Bắt đầu chèn tài khoản R0001...';

-- Mật khẩu là '123456'
INSERT INTO users (user_id, username, password, role, status) VALUES
('R0001', 'docgia03', '$2a$10$nam6Ys9hm7Ssj7/Pz516TOhVsGyYEdkJ3PgWfTg9BnSCS67Z4ibL2', 'READER', 1);
GO

INSERT INTO user_details (user_id, full_name, email, phone, address, dob, gender) VALUES
('R0001', N'Trần Văn Độc Giả', 'docgia01@email.com', '0901234567', N'111 Đường Sách, Quận Bình Thạnh, TP.HCM', '2002-10-15', N'Nam');
GO

PRINT 'Hoàn tất thêm tài khoản R0001.';

PRINT 'Bắt đầu chèn dữ liệu lịch sử mượn sách...';

-- KỊCH BẢN 1: Một phiếu mượn đã hoàn thành, trả sách ĐÚNG HẠN
DECLARE @loanId1 INT;
-- SỬA: Thêm renewal_count
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('R0001', '2025-05-01 10:00:00', '2025-05-15', 'COMPLETED', 0);
SET @loanId1 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId1, 1, 'RETURNED', '2025-05-10 14:00:00'),
    (@loanId1, 3, 'RETURNED', '2025-05-14 09:00:00');
GO

-- KỊCH BẢN 2: Một phiếu mượn đã hoàn thành, trả sách TRỄ HẠN
DECLARE @loanId2 INT;
-- SỬA: Thêm renewal_count
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('R0001', '2025-05-20 15:30:00', '2025-06-03', 'COMPLETED', 0);
SET @loanId2 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId2, 4, 'RETURNED', '2025-06-10 11:00:00'); -- Trả trễ 7 ngày
GO

-- KỊCH BẢN 3: Một phiếu mượn đang active, và đã QUÁ HẠN (tính đến hôm nay)
DECLARE @loanId3 INT;
-- SỬA: Thêm renewal_count
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('R0001', '2025-06-01 08:00:00', '2025-06-15', 'ACTIVE', 0); -- Quá hạn so với ngày hiện tại
SET @loanId3 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId3, 5, 'OVERDUE', NULL); -- Cập nhật status thành OVERDUE
GO

-- KỊCH BẢN 4: Một phiếu mượn đang active, VẪN CÒN HẠN
DECLARE @loanId4 INT;
-- SỬA: Thêm renewal_count
INSERT INTO book_loans (user_id, borrow_date, due_date, status, renewal_count)
VALUES ('R0001', '2025-06-20 11:00:00', '2025-07-04', 'ACTIVE', 0);
SET @loanId4 = SCOPE_IDENTITY();

INSERT INTO loan_items (loan_id, book_id, status, return_date)
VALUES
    (@loanId4, 9, 'BORROWED', NULL);
GO

PRINT 'Hoàn tất chèn dữ liệu lịch sử mượn sách.';



-- =================================================================
--   BƯỚC 7: TẠO DỮ LIỆU PHÍ PHẠT TỪ CÁC PHIẾU MƯỢN QUÁ HẠN
-- =================================================================
PRINT 'Bắt đầu tạo dữ liệu phí phạt...';
GO

-- Định nghĩa mức phí phạt mỗi ngày
DECLARE @LateFeePerDay FLOAT = 2000.0;

-- Chèn các khoản phí cho những sách ĐÃ TRẢ nhưng BỊ TRỄ
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
-- Điều kiện: Sách đã trả (có return_date) và ngày trả sau ngày hẹn trả
WHERE
    li.return_date IS NOT NULL
    AND li.return_date > bl.due_date
    -- Chỉ chèn nếu chưa có khoản phạt nào cho mục mượn này
    AND NOT EXISTS (SELECT 1 FROM penalty_fees pf WHERE pf.loan_item_id = li.id);

-- Chèn các khoản phí cho những sách CHƯA TRẢ và ĐÃ QUÁ HẠN (tính đến hôm nay)
INSERT INTO penalty_fees (loan_item_id, user_id, book_id, penalty_amount, overdue_days, status, created_at)
SELECT
    li.id,
    bl.user_id,
    li.book_id,
    DATEDIFF(day, bl.due_date, GETDATE()) * @LateFeePerDay AS penalty_amount,
    DATEDIFF(day, bl.due_date, GETDATE()) AS overdue_days,
    'UNPAID' AS status,
    GETDATE() AS created_at
FROM
    loan_items li
JOIN
    book_loans bl ON li.loan_id = bl.id
-- Điều kiện: Sách chưa trả (return_date là NULL) và ngày hiện tại đã qua ngày hẹn trả
WHERE
    li.return_date IS NULL
    AND bl.due_date < GETDATE()
    -- Chỉ chèn nếu chưa có khoản phạt nào cho mục mượn này
    AND NOT EXISTS (SELECT 1 FROM penalty_fees pf WHERE pf.loan_item_id = li.id);
GO

PRINT 'Hoàn tất tạo dữ liệu phí phạt.';
GO


USE LibraryDB;
GO
UPDATE users
SET role = 'ADMIN'
WHERE username = 'pupu26';
USE LibraryDB;
GO