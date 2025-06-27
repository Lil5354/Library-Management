// PATH: src/main/java/com/uef/library/dto/ColumnDTO.java
package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnDTO {
    // Tên thuộc tính trong đối tượng dữ liệu
    private String data;
    // Tên hiển thị trên header của bảng
    private String title;
}