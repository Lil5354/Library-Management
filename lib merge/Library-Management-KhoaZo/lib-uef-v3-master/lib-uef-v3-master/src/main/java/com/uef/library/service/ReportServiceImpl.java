package com.uef.library.service.impl;

import com.uef.library.dto.*;
import com.uef.library.repository.BookLoanRepository; // THAY ĐỔI
import com.uef.library.repository.BookRepository;
import com.uef.library.repository.PenaltyFeeRepository;
import com.uef.library.service.ReportService;
import lombok.RequiredArgsConstructor; // THAY ĐỔI
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor // THAY ĐỔI
public class ReportServiceImpl implements ReportService {

    private final BookLoanRepository bookLoanRepository; // THAY ĐỔI
    private final PenaltyFeeRepository penaltyFeeRepository;
    private final BookRepository bookRepository;


    @Override
    public List<TopBookDTO> findTop5BorrowedBooksForCurrentMonth() {
        LocalDate now = LocalDate.now();
        // THAY ĐỔI: Gọi phương thức từ bookLoanRepository
        return bookLoanRepository.findTopBorrowedBooks(now.getYear(), now.getMonthValue());
    }

    @Override
    public CustomReportResponseDTO generateCustomReport(CustomReportRequestDTO request) {
        List<ColumnDTO> columns = new ArrayList<>();
        List<Map<String, Object>> data;
        String reportTitle;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateRange = String.format("Từ ngày %s đến ngày %s",
                request.getStartDate().toLocalDate().format(dtf),
                request.getEndDate().toLocalDate().format(dtf));

        switch (request.getReportType()) {
            case "luot_muon_tra":
                reportTitle = "Báo cáo Thống kê Lượt Mượn - Trả Sách";
                columns.add(new ColumnDTO("bookTitle", "Tên Sách"));
                columns.add(new ColumnDTO("readerName", "Người Mượn"));
                columns.add(new ColumnDTO("borrowDate", "Ngày Mượn"));
                columns.add(new ColumnDTO("returnDate", "Ngày Trả"));
                columns.add(new ColumnDTO("status", "Trạng thái"));
                // THAY ĐỔI: Gọi phương thức từ bookLoanRepository
                data = bookLoanRepository.findLoanItemsBetweenDates(request.getStartDate(), request.getEndDate());
                break;

            case "sach_qua_han":
                reportTitle = "Báo cáo Sách Trả Quá Hạn";
                columns.add(new ColumnDTO("bookTitle", "Tên Sách"));
                columns.add(new ColumnDTO("readerName", "Người Mượn"));
                columns.add(new ColumnDTO("dueDate", "Hạn Trả"));
                columns.add(new ColumnDTO("overdueDays", "Số Ngày Trễ"));
                // THAY ĐỔI: Gọi phương thức từ bookLoanRepository
                data = bookLoanRepository.findOverdueLoanItems(LocalDate.now());
                data.forEach(row -> {
                    LocalDate dueDate = (LocalDate) row.get("dueDate");
                    if (dueDate != null) {
                        long overdueDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                        row.put("overdueDays", overdueDays > 0 ? overdueDays : 0);
                    }
                });
                dateRange = "Tính đến ngày " + LocalDate.now().format(dtf);
                break;

            case "phieu_phat":
                reportTitle = "Báo cáo Tài chính Phí Phạt";
                columns.add(new ColumnDTO("readerName", "Độc giả"));
                columns.add(new ColumnDTO("bookTitle", "Tên Sách"));
                columns.add(new ColumnDTO("amount", "Số Tiền Phạt"));
                columns.add(new ColumnDTO("status", "Trạng thái"));
                columns.add(new ColumnDTO("createdAt", "Ngày Tạo Phiếu"));
                data = penaltyFeeRepository.findPenaltiesBetweenDates(request.getStartDate(), request.getEndDate());
                break;

            case "sach_theo_the_loai":
                reportTitle = "Báo cáo Thống kê Sách theo Thể loại";
                columns.add(new ColumnDTO("categoryName", "Tên Thể Loại"));
                columns.add(new ColumnDTO("bookCount", "Số Lượng Sách"));
                data = bookRepository.countBooksByCategory();
                dateRange = "Tổng hợp toàn bộ";
                break;

            case "hoat_dong_doc_gia":
                reportTitle = "Báo cáo Thống kê Hoạt động Độc giả";
                columns.add(new ColumnDTO("readerName", "Tên Độc Giả"));
                columns.add(new ColumnDTO("borrowCount", "Số Lượt Mượn"));
                // THAY ĐỔI: Gọi phương thức từ bookLoanRepository
                data = bookLoanRepository.countLoansByUserBetweenDates(request.getStartDate(), request.getEndDate());
                break;

            default:
                throw new IllegalArgumentException("Loại báo cáo không hợp lệ: " + request.getReportType());
        }

        reportTitle += " - " + dateRange;
        return new CustomReportResponseDTO(reportTitle, columns, data);
    }

    @Override
    public ChartDataDTO getChartData(CustomReportRequestDTO request) {
        String reportType = request.getReportType();
        switch (reportType) {
            case "luot_muon_tra":
                return getBorrowingChartData(request.getStartDate(), request.getEndDate());
            default:
                throw new IllegalArgumentException("Loại báo cáo không hỗ trợ biểu đồ: " + reportType);
        }
    }

    private ChartDataDTO getBorrowingChartData(LocalDateTime startDate, LocalDateTime endDate) {
        // THAY ĐỔI: Gọi phương thức từ bookLoanRepository
        List<Map<String, Object>> dbResult = bookLoanRepository.countBookLoansPerDayBetweenDates(startDate, endDate);
        DateTimeFormatter chartLabelFormatter = DateTimeFormatter.ofPattern("dd/MM");

        Map<LocalDate, Long> countsByDate = dbResult.stream()
                .collect(Collectors.toMap(
                        row -> ((java.sql.Date) row.get("borrowDay")).toLocalDate(),
                        row -> ((Number) row.get("borrowCount")).longValue()
                ));

        List<LocalDate> dateRange = Stream.iterate(startDate.toLocalDate(), date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1)
                .collect(Collectors.toList());

        List<String> labels = dateRange.stream()
                .map(date -> date.format(chartLabelFormatter))
                .collect(Collectors.toList());

        List<Number> data = dateRange.stream()
                .map(date -> countsByDate.getOrDefault(date, 0L))
                .collect(Collectors.toList());

        ChartDatasetDTO dataset = new ChartDatasetDTO(
                "Số phiếu mượn", // Cập nhật nhãn
                data,
                "rgba(139, 69, 19, 0.5)",
                "#5C3317",
                true
        );

        return new ChartDataDTO(labels, Collections.singletonList(dataset));
    }
}