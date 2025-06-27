// PATH: src/main/java/com/uef/library/dto/ChartDataDTO.java
package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChartDataDTO {
    private List<String> labels;
    private List<ChartDatasetDTO> datasets;
}