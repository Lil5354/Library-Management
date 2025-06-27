package com.uef.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReturnRequestDTO {
    private String userId; // Mã độc giả (có thể null)
    private List<String> isbns; // Danh sách các ISBN cần trả
}