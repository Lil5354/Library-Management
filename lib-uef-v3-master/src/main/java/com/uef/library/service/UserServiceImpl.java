package com.uef.library.service;

import com.uef.library.config.UserSpecification;
import com.uef.library.model.User;
import com.uef.library.repository.UserDetailRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailRepository userDetailRepository;

    @Override
    public long countAdmins() {
        return userRepository.countByRoleAndStatus("ADMIN", true);
    }

    @Override
    public long countStaffs() {
        return userRepository.countByRoleAndStatus("STAFF", true);
    }

    @Override
    public long countReaders() {
        return userRepository.countByRoleAndStatus("READER", true);
    }

    @Override
    public long countLockedAccounts() {
        return userRepository.countByStatus(false);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllWithDetails() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUserDetail() != null) {
                user.getUserDetail().getId();
            }
        }
        return users;
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email, String currentUsername) {
        Optional<User> userByEmail = userRepository.findByUserDetail_Email(email);
        return userByEmail.isPresent() && !userByEmail.get().getUsername().equals(currentUsername);
    }

    // === PHẦN TRIỂN KHAI MỚI ===
    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(String role) {
        // Gọi trực tiếp phương thức từ repository đã có sẵn
        return userRepository.findByRole(role);
    }
    // ===========================

    @Override
    @Transactional
    public String registerUser(String username, String rawPassword) {
        if (isUsernameTaken(username)) {
            return "Tên đăng nhập đã tồn tại.";
        }

        String newUserId = generateNextUserId();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .userId(newUserId)
                .username(username)
                .password(hashedPassword)
                .role("READER")
                .status(true)
                .build();

        userRepository.save(user);
        return "success";
    }

    private String generateNextUserId() {
        String lastId = userRepository.findLastUserId();
        int nextNumber = 1;

        if (lastId != null && lastId.matches("U\\d{4}")) {
            nextNumber = Integer.parseInt(lastId.substring(1)) + 1;
        }

        return String.format("U%04d", nextNumber);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public boolean updateUserRole(String userId, String newRole) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if ("ADMIN".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(newRole)) {
                System.err.println("Không thể thay đổi vai trò của tài khoản ADMIN chính.");
                return false;
            }
            if ("ADMIN".equalsIgnoreCase(newRole) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                System.err.println("Không thể gán vai trò ADMIN qua chức năng này.");
                return false;
            }
            user.setRole(newRole.toUpperCase());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteUser(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User userToDelete = optionalUser.get();

            if ("ADMIN".equalsIgnoreCase(userToDelete.getRole())) {
                System.err.println("Không thể xóa tài khoản ADMIN: " + userId);
                return false;
            }
            userRepository.delete(userToDelete);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean saveUser(User user) {
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu user: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findPaginatedUsersWithFilter(String keyword, String role, Boolean status, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterGeneric(keyword, role, status);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        userPage.getContent().forEach(user -> {
            if (user.getUserDetail() != null) {
                user.getUserDetail().getId();
            }
        });
        return userPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            if (user.getUserDetail() != null) {
                user.getUserDetail().getId();
            }
        });
        return userOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean phoneExistsForOtherUser(String phone, String currentUserId) {
        // Gọi phương thức mới từ repository
        return userDetailRepository.existsByPhoneAndUser_UserIdNot(phone, currentUserId);
    }
}