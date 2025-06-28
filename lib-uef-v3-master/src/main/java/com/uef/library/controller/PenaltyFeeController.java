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

    @GetMapping
    public ResponseEntity<Page<PenaltyFeeDTO>> getAllPenaltyFees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size, // Tăng size để lấy đủ dữ liệu cho việc nhóm ở frontend
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "") String search) { // Thêm tham số search

        // Logic sort giữ nguyên
        String sortField = sort.length > 0 ? sort[0] : "createdAt";
        Sort.Direction direction = sort.length > 1 ? Sort.Direction.fromString(sort[1]) : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Gọi service với đầy đủ tham số
        Page<PenaltyFeeDTO> penaltyFees = penaltyFeeService.getAllPenaltyFees(status, search, pageable);
        return ResponseEntity.ok(penaltyFees);
    }

    @PostMapping("/pay/{penaltyId}")
    public ResponseEntity<?> markAsPaid(@PathVariable Long penaltyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String staffUsername = authentication.getName();
            PenaltyFeeDTO updatedPenalty = penaltyFeeService.markPenaltyAsPaid(penaltyId, staffUsername);
            return ResponseEntity.ok(updatedPenalty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/waive/{penaltyId}")
    public ResponseEntity<?> waivePenalty(@PathVariable Long penaltyId, @RequestBody(required = false) Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String staffUserId = authentication.getName();
            String reason = (payload != null) ? payload.get("reason") : "";

            PenaltyFeeDTO updatedPenalty = penaltyFeeService.waivePenaltyFee(penaltyId, reason, staffUserId);
            return ResponseEntity.ok(updatedPenalty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{penaltyId}")
    public ResponseEntity<?> getPenaltyDetail(@PathVariable Long penaltyId) {
        try {
            PenaltyFeeDTO penaltyDetail = penaltyFeeService.getPenaltyFeeById(penaltyId);
            return ResponseEntity.ok(penaltyDetail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}