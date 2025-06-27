package com.uef.library.dto;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CustomReportRequestDTO {
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
