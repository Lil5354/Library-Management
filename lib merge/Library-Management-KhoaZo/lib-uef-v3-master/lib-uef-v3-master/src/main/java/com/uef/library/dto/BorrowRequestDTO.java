package com.uef.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class BorrowRequestDTO {
    private String userId; // Mã của độc giả mượn sách
    private List<String> bookIsbns; // Danh sách ISBN của các sách được mượn
}