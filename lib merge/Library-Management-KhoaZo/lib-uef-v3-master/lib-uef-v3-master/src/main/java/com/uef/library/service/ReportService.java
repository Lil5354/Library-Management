// PATH: src/main/java/com/uef/library/service/ReportService.java
package com.uef.library.service;

import com.uef.library.dto.ChartDataDTO;
import com.uef.library.dto.CustomReportRequestDTO;
import com.uef.library.dto.CustomReportResponseDTO;
import com.uef.library.dto.TopBookDTO;

import java.util.List;

public interface ReportService {
    /**
     * Lấy danh sách top 5 sách được mượn nhiều nhất trong tháng hiện tại.
     * @return Danh sách các DTO chứa thông tin sách và lượt mượn.
     */
    List<TopBookDTO> findTop5BorrowedBooksForCurrentMonth();

    /**
     * Tạo báo cáo tùy chỉnh dạng bảng để xuất file.
     * @param request DTO chứa loại báo cáo và khoảng thời gian.
     * @return DTO chứa định dạng cột và dữ liệu báo cáo.
     */
    CustomReportResponseDTO generateCustomReport(CustomReportRequestDTO request);

    /**
     * Lấy dữ liệu đã được xử lý cho việc vẽ biểu đồ.
     * @param request DTO chứa loại báo cáo và khoảng thời gian.
     * @return DTO chứa labels và datasets cho Chart.js.
     */
    ChartDataDTO getChartData(CustomReportRequestDTO request);
}