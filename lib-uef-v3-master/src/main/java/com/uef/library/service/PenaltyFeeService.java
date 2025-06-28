package com.uef.library.service;

import com.uef.library.dto.PenaltyFeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PenaltyFeeService {

    Page<PenaltyFeeDTO> getAllPenaltyFees(String status, String search, Pageable pageable);

    PenaltyFeeDTO markPenaltyAsPaid(Long penaltyId, String staffUserId) throws Exception;

    PenaltyFeeDTO waivePenaltyFee(Long penaltyId, String reason, String staffUserId) throws Exception;

    PenaltyFeeDTO getPenaltyFeeById(Long penaltyId) throws Exception;

    PenaltyFeeDTO createPenaltyFeeForOverdueBorrowing(Long borrowingId, int overdueDays, Double penaltyAmount) throws Exception;

    boolean hasUnpaidPenalty(Long borrowingId);
}