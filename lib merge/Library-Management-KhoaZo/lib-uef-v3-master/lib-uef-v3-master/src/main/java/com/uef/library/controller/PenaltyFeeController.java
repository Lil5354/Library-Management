package com.uef.library.controller.api;

import com.uef.library.dto.PenaltyFeeDTO;
import com.uef.library.service.PenaltyFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/penalty-fees")
@RequiredArgsConstructor
public class PenaltyFeeController {

    private final PenaltyFeeService penaltyFeeService;

    // Endpoint để lấy danh sách phí phạt với phân trang và lọc trạng thái
    @GetMapping
    public ResponseEntity<Page<PenaltyFeeDTO>> getAllPenaltyFees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String status) {

        Sort.Direction direction = sort.length > 1 ? Sort.Direction.fromString(sort[1]) : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<PenaltyFeeDTO> penaltyFees = penaltyFeeService.getAllPenaltyFees(status, pageable);
        return ResponseEntity.ok(penaltyFees);
    }

    // === START: ENDPOINT MỚI ĐƯỢC THÊM VÀO ĐỂ SỬA LỖI ===
    /**
     * Endpoint để xử lý việc đánh dấu một khoản phạt đã được thu tiền mặt.
     * Được gọi khi thủ thư nhấn nút "Đã thu" (biểu tượng tờ tiền).
     *
     * @param penaltyId ID của khoản phạt cần xử lý.
     * @return DTO của khoản phạt đã được cập nhật.
     */
    @PostMapping("/pay/{penaltyId}")
    public ResponseEntity<?> markAsPaid(@PathVariable Long penaltyId) {
        try {
            // Lấy thông tin người dùng đang đăng nhập (thủ thư)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String staffUsername = authentication.getName();

            // Gọi service để thực hiện logic nghiệp vụ
            PenaltyFeeDTO updatedPenalty = penaltyFeeService.markPenaltyAsPaid(penaltyId, staffUsername);
            return ResponseEntity.ok(updatedPenalty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // === END: ENDPOINT MỚI ĐƯỢC THÊM VÀO ĐỂ SỬA LỖI ===


    // Endpoint để miễn giảm phí phạt
    @PostMapping("/waive/{penaltyId}")
    public ResponseEntity<?> waivePenalty(@PathVariable Long penaltyId, @RequestBody(required = false) Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String staffUserId = authentication.getName();
            String reason = (payload != null) ? payload.get("reason") : "";

            PenaltyFeeDTO updatedPenalty = penaltyFeeService.waivePenaltyFee(penaltyId, reason, staffUserId);
            return ResponseEntity.ok(updatedPenalty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint để lấy chi tiết một khoản phí phạt
    @GetMapping("/{penaltyId}")
    public ResponseEntity<?> getPenaltyDetail(@PathVariable Long penaltyId) {
        try {
            PenaltyFeeDTO penaltyDetail = penaltyFeeService.getPenaltyFeeById(penaltyId);
            return ResponseEntity.ok(penaltyDetail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Endpoint để polling
    @GetMapping("/{penaltyId}/status")
    public ResponseEntity<?> getPenaltyStatus(@PathVariable Long penaltyId) {
        try {
            PenaltyFeeDTO penalty = penaltyFeeService.getPenaltyFeeById(penaltyId);
            return ResponseEntity.ok(Map.of("status", penalty.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
