package com.uef.library.dto;

// Sử dụng Lombok để code ngắn gọn, nếu chưa có thì thêm dependency vào pom.xml
// Hoặc tự tạo constructor, getter, setter
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopBookDTO {
    private String title;
    private Long borrowCount;
}