package com.uef.library.service;

import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    long countAdmins();
    long countStaffs();
    long countReaders();
    long countLockedAccounts();

    List<User> findAllUsers();
    List<User> findAllWithDetails();

    boolean usernameExists(String username);
    boolean isUsernameTaken(String username);

    boolean emailExists(String email, String currentUsername);

    /**
     * Tìm tất cả người dùng theo một vai trò (role) cụ thể.
     * @param role Vai trò cần tìm (VD: "STAFF", "READER").
     * @return Danh sách các User có vai trò tương ứng.
     */
    List<User> findByRole(String role);

    String registerUser(String username, String rawPassword);
    Optional<User> findByUsername(String username);
    boolean updateUserRole(String userId, String newRole);
    Optional<User> findById(String userId);
    boolean deleteUser(String userId);
    boolean saveUser(User user);
    Page<User> findPaginatedUsersWithFilter(String keyword, String role, Boolean status, Pageable pageable);
}