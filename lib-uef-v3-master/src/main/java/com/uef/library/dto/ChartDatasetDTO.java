// PATH: src/main/java/com/uef/library/dto/ChartDatasetDTO.java
package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChartDatasetDTO {
    private String label;
    private List<Number> data;
    private String backgroundColor;
    private String borderColor;
    private boolean tension = false; // Đặt thành true để có đường cong
}