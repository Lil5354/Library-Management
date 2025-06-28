package com.uef.library.service;

import com.uef.library.dto.StaffProfileDTO;
import com.uef.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    List<User> findByRole(String role);
    boolean phoneExistsForOtherUser(String phone, String currentUserId);

    String registerUser(String username, String rawPassword);
    Optional<User> findByUsername(String username);
    boolean updateUserRole(String userId, String newRole);
    Optional<User> findById(String userId);
    boolean deleteUser(String userId);
    boolean saveUser(User user);
    Page<User> findPaginatedUsersWithFilter(String keyword, String role, Boolean status, Pageable pageable);

    // ===== PHƯƠNG THỨC MỚI ĐÃ THÊM =====
    User updateStaffProfile(String username, StaffProfileDTO profileDTO) throws Exception;
    User updateStaffAvatar(String username, MultipartFile avatarFile) throws IOException;
}