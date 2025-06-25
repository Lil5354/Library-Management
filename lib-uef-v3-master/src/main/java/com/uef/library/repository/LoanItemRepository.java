package com.uef.library.repository;
import com.uef.library.model.LoanItem;
import com.uef.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {
    @Query("SELECT count(li) FROM LoanItem li WHERE li.bookLoan.user = :user AND li.status = 'BORROWED'")
    long countActiveLoansByUser(User user);
    List<LoanItem> findByBookLoan_UserAndStatus(User user, String status);
    Page<LoanItem> findByBookLoan_UserOrderByBookLoan_BorrowDateDesc(User user, Pageable pageable);

}