package com.uef.library.repository;

import com.uef.library.model.LoanItem;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 2 (Hữu ích) >>>
    @Query("SELECT count(li) FROM LoanItem li WHERE li.bookLoan.user = :user AND li.status = 'BORROWED'")
    long countActiveLoansByUser(@Param("user") User user);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 2 (Hữu ích) >>>
    List<LoanItem> findByBookLoan_UserAndStatus(User user, String status);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 2 (Hữu ích) >>>
    Page<LoanItem> findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(User user, Pageable pageable);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 1 (Cho nghiệp vụ trả sách của thủ thư) >>>
    @Query("SELECT li FROM LoanItem li WHERE li.bookLoan.user.userId = :userId AND li.book.isbn = :isbn AND li.status = 'BORROWED'")
    Optional<LoanItem> findActiveLoanItemByUserIdAndBookIsbn(@Param("userId") String userId, @Param("isbn") String isbn);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 1 (Cho nghiệp vụ trả sách của thủ thư) >>>
    Optional<LoanItem> findFirstByBookIsbnAndStatus(String isbn, String status);

    // <<< MERGE: GIỮ LẠI TỪ ĐỒ ÁN 1 (Cho nghiệp vụ trả sách của thủ thư) >>>
    List<LoanItem> findByBookLoanId(Long bookLoanId);
}