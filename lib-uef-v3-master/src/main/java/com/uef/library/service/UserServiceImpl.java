package com.uef.library.service;

import com.uef.library.config.UserSpecification;
import com.uef.library.dto.StaffProfileDTO;
import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.repository.UserDetailRepository;
import com.uef.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

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
    @Transactional
    public User updateStaffProfile(String username, StaffProfileDTO profileDTO) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng: " + username));

        UserDetail userDetail = user.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setUser(user);
        }

        userDetail.setFullName(profileDTO.getFullName());
        userDetail.setEmail(profileDTO.getEmail());
        userDetail.setPhone(profileDTO.getPhone());
        userDetail.setAddress(profileDTO.getAddress());

        userDetailRepository.save(userDetail);
        user.setUserDetail(userDetail);
        return user;
    }

    @Override
    @Transactional
    public User updateStaffAvatar(String username, MultipartFile avatarFile) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        UserDetail userDetail = user.getUserDetail();
        if (userDetail == null) {
            userDetail = new UserDetail();
            userDetail.setUser(user);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveFile(avatarFile, "avatars");
            userDetail.setAvatar(avatarUrl);
        }

        userDetailRepository.save(userDetail);
        user.setUserDetail(userDetail);
        return user;
    }

    private String saveFile(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Path uploadPath = Paths.get("uploads", subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(uniqueFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/" + "uploads" + "/" + subDir + "/" + uniqueFilename;
    }
}