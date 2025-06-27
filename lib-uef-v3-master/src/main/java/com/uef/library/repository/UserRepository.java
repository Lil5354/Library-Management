package com.uef.library.repository;

import com.uef.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    long countByRoleAndStatus(String role, boolean status);

    long countByStatus(boolean status);

    boolean existsByUsername(String username);

    @Query(value = "SELECT u.user_id FROM users u ORDER BY u.user_id DESC LIMIT 1", nativeQuery = true)
    String findLastUserId();

    Optional<User> findByUsername(String username);

    List<User> findByRole(String role);

    // PHƯƠNG THỨC CẦN THIẾT CHO VIỆC KIỂM TRA EMAIL
    Optional<User> findByUserDetail_Email(String email);

    // Bạn cũng có thể giữ lại phương thức này nếu cần
    boolean existsByUserDetail_Email(String email);
}