package com.uef.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnResponseDTO {
    private LoanItemDTO returnedItem; // <-- Sửa ở đây
    private PenaltyFeeDTO penaltyFee;
}

