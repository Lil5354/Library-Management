package com.uef.library.service;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import com.uef.library.config.UserSpecification;
import com.uef.library.dto.ReaderDetailDTO;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.repository.UserRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ReaderManagementServiceImpl implements ReaderManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String READER_ROLE = "READER";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public Page<ReaderDetailDTO> getPaginatedReaders(String keyword, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterReaders(keyword, READER_ROLE, null);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::convertToDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReaderDetailDTO> findReaderById(String userId) {
        return userRepository.findById(userId).map(this::convertToDetailDto);
    }

    /**
     * SỬA LỖI: Thay đổi kiểu trả về và trả về DTO sau khi tạo
     */
    @Override
    public ReaderDetailDTO createReader(ReaderDetailDTO readerDto) throws Exception {
        if (userRepository.existsById(readerDto.getUserId())) {
            throw new Exception("Mã độc giả '" + readerDto.getUserId() + "' đã tồn tại.");
        }
        if (userRepository.existsByUsername(readerDto.getUsername())) {
            throw new Exception("Tên đăng nhập '" + readerDto.getUsername() + "' đã tồn tại.");
        }

        User user = new User();
        user.setUserId(readerDto.getUserId());
        user.setUsername(readerDto.getUsername());
        user.setPassword(passwordEncoder.encode(readerDto.getPassword()));
        user.setRole(READER_ROLE);
        user.setStatus(true);

        UserDetail userDetail = new UserDetail();
        userDetail.setFullName(readerDto.getFullName());
        userDetail.setEmail(readerDto.getEmail());
        userDetail.setPhone(readerDto.getPhone());
        userDetail.setAddress(readerDto.getAddress());
        userDetail.setDob(readerDto.getDob());
        userDetail.setGender(readerDto.getGender());
        userDetail.setMembershipExpiryDate(LocalDate.now().plusYears(1));

        user.setUserDetail(userDetail);
        userDetail.setUser(user);

        // Lưu người dùng và lấy đối tượng đã được lưu
        User savedUser = userRepository.save(user);

        // Chuyển đổi và trả về DTO của người dùng vừa tạo
        return convertToDetailDto(savedUser);
    }

    /**
     * SỬA LỖI: Thay đổi kiểu trả về và trả về DTO sau khi cập nhật
     */
    @Override
    public ReaderDetailDTO updateReader(String userId, ReaderDetailDTO readerDto) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Không tìm thấy độc giả với mã: " + userId));

        UserDetail userDetail = user.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setUser(user);
            user.setUserDetail(userDetail);
        }

        // Cập nhật thông tin từ DTO
        userDetail.setFullName(readerDto.getFullName());
        userDetail.setEmail(readerDto.getEmail());
        user.setStatus(readerDto.isStatus());
        if (readerDto.getPhone() != null) userDetail.setPhone(readerDto.getPhone());
        if (readerDto.getAddress() != null) userDetail.setAddress(readerDto.getAddress());
        if (readerDto.getMembershipExpiryDate() != null) userDetail.setMembershipExpiryDate(readerDto.getMembershipExpiryDate());
        if (readerDto.getDob() != null) userDetail.setDob(readerDto.getDob());
        if (readerDto.getGender() != null) userDetail.setGender(readerDto.getGender());
        if (readerDto.getAvatar() != null) userDetail.setAvatar(readerDto.getAvatar());

        // Lưu và lấy đối tượng đã được cập nhật
        User updatedUser = userRepository.save(user);

        // Chuyển đổi và trả về DTO của người dùng vừa cập nhật
        return convertToDetailDto(updatedUser);
    }

    @Override
    public void deleteReader(String userId) {
        userRepository.deleteById(userId);
    }

    // --- CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN ---
    @Override
    @Transactional
    public void importReadersFromExcel(MultipartFile file) throws IOException {
        List<User> usersToSave = new ArrayList<>();
        Set<String> processedUserIds = new HashSet<>();
        Set<String> processedUsernames = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Bỏ qua dòng tiêu đề (header)
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String userId = getCellValueAsString(row.getCell(0));
                String username = getCellValueAsString(row.getCell(1));
                String fullName = getCellValueAsString(row.getCell(2));

                if (userId.isEmpty() || username.isEmpty() || fullName.isEmpty()) {
                    continue;
                }
                if (userRepository.existsById(userId) || processedUserIds.contains(userId) ||
                        userRepository.existsByUsername(username) || processedUsernames.contains(username)) {
                    System.out.println("Bỏ qua độc giả đã tồn tại: userId=" + userId + ", username=" + username);
                    continue;
                }

                processedUserIds.add(userId);
                processedUsernames.add(username);

                User user = new User();
                user.setUserId(userId);
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(READER_ROLE);
                user.setStatus(true);

                UserDetail detail = new UserDetail();
                detail.setFullName(fullName);
                detail.setEmail(getCellValueAsString(row.getCell(3)));
                detail.setPhone(getCellValueAsString(row.getCell(4)));
                detail.setGender(getCellValueAsString(row.getCell(5)));
                detail.setAddress(getCellValueAsString(row.getCell(7)));

                try {
                    String dobStr = getCellValueAsString(row.getCell(6));
                    if (!dobStr.isEmpty()) detail.setDob(LocalDate.parse(dobStr, DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    System.err.println("Lỗi định dạng ngày sinh ở dòng có mã: " + userId + ". Bỏ qua dòng này.");
                }

                try {
                    String expiryDateStr = getCellValueAsString(row.getCell(8));
                    if (!expiryDateStr.isEmpty()) detail.setMembershipExpiryDate(LocalDate.parse(expiryDateStr, DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    System.err.println("Lỗi định dạng ngày hết hạn ở dòng có mã: " + userId + ". Bỏ qua dòng này.");
                }

                user.setUserDetail(detail);
                detail.setUser(user);
                usersToSave.add(user);
            }
        } catch (Exception e) {
            throw new IOException("File không hợp lệ hoặc định dạng bị lỗi. Vui lòng kiểm tra lại file Excel.", e);
        }

        // --- BỔ SUNG LOGIC QUAN TRỌNG TẠI ĐÂY ---
        if (usersToSave.isEmpty()) {
            // Ném ra lỗi để Controller có thể bắt và thông báo cho người dùng
            throw new IllegalArgumentException("Không tìm thấy dữ liệu độc giả hợp lệ nào để import. Vui lòng kiểm tra file Excel.");
        }

        userRepository.saveAll(usersToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportReadersToExcel(HttpServletResponse response) throws IOException {
        List<User> readers = userRepository.findByRole(READER_ROLE);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Danh sách Độc giả");
            createHeaderRow(sheet, workbook);

            int rowNum = 1;
            for (User user : readers) {
                Row row = sheet.createRow(rowNum++);
                UserDetail detail = user.getUserDetail();

                row.createCell(0).setCellValue(user.getUserId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(detail != null ? detail.getFullName() : "");
                row.createCell(3).setCellValue(detail != null ? detail.getEmail() : "");
                row.createCell(4).setCellValue(detail != null ? detail.getPhone() : "");
                row.createCell(5).setCellValue(detail != null ? detail.getGender() : "");
                row.createCell(6).setCellValue(detail != null && detail.getDob() != null ? DATE_FORMATTER.format(detail.getDob()) : "");
                row.createCell(7).setCellValue(detail != null ? detail.getAddress() : "");
                row.createCell(8).setCellValue(detail != null && detail.getMembershipExpiryDate() != null ? DATE_FORMATTER.format(detail.getMembershipExpiryDate()) : "");
                row.createCell(9).setCellValue(user.isStatus() ? "Hoạt động" : "Bị khóa");
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMATTER.format(cell.getLocalDateTimeCellValue());
                }
                return new DataFormatter().formatCellValue(cell);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private void createHeaderRow(XSSFSheet sheet, XSSFWorkbook workbook) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        headerStyle.setFont(font);

        String[] headers = {"Mã Độc Giả", "Tên Đăng Nhập", "Họ và Tên", "Email", "SĐT", "Giới tính", "Ngày sinh", "Địa chỉ", "Ngày Hết Hạn", "Trạng Thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private ReaderDetailDTO convertToDetailDto(User user) {
        UserDetail detail = user.getUserDetail();
        return ReaderDetailDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.isStatus())
                .fullName(detail != null ? detail.getFullName() : null)
                .email(detail != null ? detail.getEmail() : null)
                .phone(detail != null ? detail.getPhone() : null)
                .address(detail != null ? detail.getAddress() : null)
                .gender(detail != null ? detail.getGender() : null)
                .dob(detail != null ? detail.getDob() : null)
                .membershipExpiryDate(detail != null ? detail.getMembershipExpiryDate() : null)
                .avatar(detail != null ? detail.getAvatar() : null)
                .build();
    }
}