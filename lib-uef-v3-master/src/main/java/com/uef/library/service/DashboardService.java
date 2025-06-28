package com.uef.library.service;

import com.uef.library.dto.DashboardStatsDTO;
import com.uef.library.repository.BookLoanRepository; // Đảm bảo import này
import com.uef.library.repository.LoanItemRepository; // Đảm bảo import này
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime; // Thêm import này
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LoanItemRepository loanItemRepository;
    private final BookLoanRepository bookLoanRepository; // Đảm bảo đã inject BookLoanRepository

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 00:00:00 của ngày hôm nay
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 23:59:59.999999999 của ngày hôm nay

        // 1. Tổng số sách đang mượn (đang hoạt động)
        stats.setBorrowedBooks(loanItemRepository.countByStatusIn(Arrays.asList("BORROWED", "OVERDUE")));

        // 2. Số sách quá hạn
        stats.setOverdueBooks(loanItemRepository.countOverdueLoans());

        // 3. Lượt mượn trong ngày hôm nay
        stats.setBorrowsToday(bookLoanRepository.countByBorrowDateBetween(startOfDay, endOfDay));

        // 4. Lượt trả trong ngày hôm nay
        stats.setReturnsToday(loanItemRepository.countByReturnDateBetween(startOfDay, endOfDay));

        // Note: newReadersToday cần được tính toán từ ReaderManagementService hoặc UserRepository
        // (Nếu bạn có UserRepository và User.role='READER', bạn có thể thêm:
        // stats.setNewReadersToday(userRepository.countByRoleAndCreatedAtBetween("READER", startOfDay, endOfDay));
        // Đảm bảo có phương thức đó trong UserRepository và inject UserRepository vào đây)
        // Hiện tại tôi sẽ để mặc định là 0 hoặc bạn có thể tự thêm logic.
        stats.setNewReadersToday(0); // Giả sử nếu chưa có logic cụ thể

        return stats;
    }
}