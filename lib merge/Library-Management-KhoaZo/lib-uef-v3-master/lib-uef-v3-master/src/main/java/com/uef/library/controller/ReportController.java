// PATH: src/main/java/com/uef/library/controller/ReportController.java
package com.uef.library.controller;

import com.uef.library.dto.*;
import com.uef.library.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/top-borrowed-books")
    public ResponseEntity<List<TopBookDTO>> getTopBorrowedBooks() {
        List<TopBookDTO> topBooks = reportService.findTop5BorrowedBooksForCurrentMonth();
        return ResponseEntity.ok(topBooks);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@RequestBody CustomReportRequestDTO request) {
        if (request.getReportType() == null || request.getStartDate() == null || request.getEndDate() == null) {
            ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Vui lòng cung cấp đủ thông tin: loại báo cáo, ngày bắt đầu và ngày kết thúc.");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Ngày bắt đầu không được sau ngày kết thúc.");
            return ResponseEntity.badRequest().body(error);
        }

        CustomReportResponseDTO response = reportService.generateCustomReport(request);
        return ResponseEntity.ok(response);
    }

    // --- ENDPOINT MỚI CHO BIỂU ĐỒ ---
    @PostMapping("/chart-data")
    public ResponseEntity<?> getChartData(@RequestBody CustomReportRequestDTO request) {
        if (request.getReportType() == null || request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Vui lòng chọn loại báo cáo và khoảng thời gian."));
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Ngày bắt đầu không được sau ngày kết thúc."));
        }
        try {
            ChartDataDTO chartData = reportService.getChartData(request);
            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}