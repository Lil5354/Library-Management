package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomReportResponseDTO {
    private String title; // <-- THÊM DÒNG NÀY
    private List<ColumnDTO> columns;
    // Sử dụng Map để linh hoạt cho nhiều loại báo cáo khác nhau
    private List<Map<String, Object>> data;
}
