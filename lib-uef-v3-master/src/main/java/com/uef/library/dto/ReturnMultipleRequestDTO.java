package com.uef.library.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReturnMultipleRequestDTO {
    // <<< THAY ĐỔI: Chuyển từ userId sang loanId >>>
    private Long loanId;
    private List<String> isbns;
}