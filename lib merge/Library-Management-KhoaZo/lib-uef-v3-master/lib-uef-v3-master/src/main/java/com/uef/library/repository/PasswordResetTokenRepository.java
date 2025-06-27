package com.uef.library.repository;

import com.uef.library.model.PasswordResetToken;
import com.uef.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user); // <<< THÊM DÒNG NÀY
}