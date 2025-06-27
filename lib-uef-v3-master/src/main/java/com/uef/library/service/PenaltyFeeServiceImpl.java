package com.uef.library.service;

import com.uef.library.dto.PenaltyFeeDTO;
import com.uef.library.model.*;
import com.uef.library.repository.LoanItemRepository; // THAY ĐỔI
import com.uef.library.repository.PenaltyFeeRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PenaltyFeeServiceImpl implements PenaltyFeeService {

    private final PenaltyFeeRepository penaltyFeeRepository;
    private final LoanItemRepository loanItemRepository; // THAY ĐỔI
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PenaltyFeeDTO> getAllPenaltyFees(String status, Pageable pageable) {
        Page<PenaltyFee> penaltyFeesPage;
        if (status != null && !status.isEmpty()) {
            try {
                PenaltyStatus penaltyStatus = PenaltyStatus.valueOf(status.toUpperCase());
                penaltyFeesPage = penaltyFeeRepository.findByStatus(penaltyStatus, pageable);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        } else {
            penaltyFeesPage = penaltyFeeRepository.findAll(pageable);
        }
        return penaltyFeesPage.map(this::convertToDto);
    }

    @Override
    @Transactional
    public PenaltyFeeDTO markPenaltyAsPaid(Long penaltyId, String staffUserId) throws Exception {
        PenaltyFee penaltyFee = penaltyFeeRepository.findById(penaltyId)
                .orElseThrow(() -> new Exception("Không tìm thấy phí phạt với ID: " + penaltyId));

        if (penaltyFee.getStatus() == PenaltyStatus.PAID) {
            throw new Exception("Phí phạt này đã được thanh toán trước đó.");
        }
        if (penaltyFee.getStatus() == PenaltyStatus.WAIVED) {
            throw new Exception("Phí phạt này đã được miễn giảm, không thể đánh dấu đã thu.");
        }

        User staffUser = userRepository.findByUsername(staffUserId)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin thủ thư."));

        penaltyFee.setStatus(PenaltyStatus.PAID);
        penaltyFee.setPaidAt(LocalDateTime.now());
        penaltyFee.setCollectedByUser(staffUser);
        PenaltyFee updatedPenalty = penaltyFeeRepository.save(penaltyFee);

        return convertToDto(updatedPenalty);
    }

    @Override
    @Transactional
    public PenaltyFeeDTO waivePenaltyFee(Long penaltyId, String reason, String staffUserId) throws Exception {
        PenaltyFee penaltyFee = penaltyFeeRepository.findById(penaltyId)
                .orElseThrow(() -> new Exception("Không tìm thấy phí phạt với ID: " + penaltyId));

        if (penaltyFee.getStatus() == PenaltyStatus.PAID) {
            throw new Exception("Phí phạt này đã được thanh toán, không thể miễn giảm.");
        }
        if (penaltyFee.getStatus() == PenaltyStatus.WAIVED) {
            throw new Exception("Phí phạt này đã được miễn giảm trước đó.");
        }

        User staffUser = userRepository.findByUsername(staffUserId)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin thủ thư."));

        penaltyFee.setStatus(PenaltyStatus.WAIVED);
        penaltyFee.setWaivedReason(reason);
        penaltyFee.setCollectedByUser(staffUser);
        PenaltyFee updatedPenalty = penaltyFeeRepository.save(penaltyFee);

        return convertToDto(updatedPenalty);
    }

    @Override
    @Transactional(readOnly = true)
    public PenaltyFeeDTO getPenaltyFeeById(Long penaltyId) throws Exception {
        PenaltyFee penaltyFee = penaltyFeeRepository.findById(penaltyId)
                .orElseThrow(() -> new Exception("Không tìm thấy phí phạt với ID: " + penaltyId));
        return convertToDto(penaltyFee);
    }

    // === THAY ĐỔI: Nhận vào loanItemId thay vì borrowingId ===
    @Override
    @Transactional
    public PenaltyFeeDTO createPenaltyFeeForOverdueBorrowing(Long loanItemId, int overdueDays, Double penaltyAmount) throws Exception {
        LoanItem loanItem = loanItemRepository.findById(loanItemId)
                .orElseThrow(() -> new Exception("Không tìm thấy chi tiết mượn với ID: " + loanItemId));

        if (penaltyFeeRepository.findByLoanItemIdAndStatus(loanItemId, PenaltyStatus.UNPAID).isPresent()) {
            throw new Exception("Khoản phạt chưa thanh toán cho lượt mượn sách này đã tồn tại.");
        }

        PenaltyFee penaltyFee = PenaltyFee.builder()
                .loanItem(loanItem)
                .user(loanItem.getBookLoan().getUser())
                .book(loanItem.getBook())
                .overdueDays(overdueDays)
                .penaltyAmount(penaltyAmount)
                .status(PenaltyStatus.UNPAID)
                .build();
        PenaltyFee savedPenalty = penaltyFeeRepository.save(penaltyFee);
        return convertToDto(savedPenalty);
    }

    // === THAY ĐỔI: Kiểm tra theo loanItemId ===
    @Override
    @Transactional(readOnly = true)
    public boolean hasUnpaidPenalty(Long loanItemId) {
        return penaltyFeeRepository.findByLoanItemIdAndStatus(loanItemId, PenaltyStatus.UNPAID).isPresent();
    }
    // ===========================================

    private PenaltyFeeDTO convertToDto(PenaltyFee penaltyFee) {
        String readerName = "N/A";
        String readerId = "N/A";
        if (penaltyFee.getUser() != null) {
            readerId = penaltyFee.getUser().getUserId();
            if (penaltyFee.getUser().getUserDetail() != null) {
                readerName = penaltyFee.getUser().getUserDetail().getFullName();
            }
        }

        String bookTitle = "N/A";
        String bookIsbn = "N/A";
        Long bookId = null;
        if (penaltyFee.getBook() != null) {
            bookTitle = penaltyFee.getBook().getTitle();
            bookIsbn = penaltyFee.getBook().getIsbn();
            bookId = penaltyFee.getBook().getId();
        }

        String collectedByUserName = "N/A";
        if (penaltyFee.getCollectedByUser() != null && penaltyFee.getCollectedByUser().getUserDetail() != null) {
            collectedByUserName = penaltyFee.getCollectedByUser().getUserDetail().getFullName();
        }

        // === THAY ĐỔI: Sử dụng loanItem.id thay vì borrowing.id ===
        Long borrowingId = (penaltyFee.getLoanItem() != null) ? penaltyFee.getLoanItem().getId() : null;

        return PenaltyFeeDTO.builder()
                .id(penaltyFee.getId())
                .borrowingId(borrowingId) // Giữ tên trường borrowingId trong DTO để tương thích frontend nếu cần
                .readerId(readerId)
                .readerName(readerName)
                .bookId(bookId)
                .bookTitle(bookTitle)
                .bookIsbn(bookIsbn)
                .overdueDays(penaltyFee.getOverdueDays())
                .penaltyAmount(penaltyFee.getPenaltyAmount())
                .status(penaltyFee.getStatus().name())
                .createdAt(penaltyFee.getCreatedAt())
                .paidAt(penaltyFee.getPaidAt())
                .waivedReason(penaltyFee.getWaivedReason())
                .collectedByUserName(collectedByUserName)
                .build();
    }
}