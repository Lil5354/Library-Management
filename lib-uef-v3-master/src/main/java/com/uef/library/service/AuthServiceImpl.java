package com.uef.library.service;

import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
import com.uef.library.repository.UserDetailRepository;
import com.uef.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Override
    public void registerUser(String username, String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);

        // Tạo UserDetail mặc định
        UserDetail userDetail = UserDetail.builder()
                .fullName("Chưa cập nhật")
                .email("Chưa cập nhật")
                .phone("Chưa cập nhật")
                .address("Chưa cập nhật")
                .dob(LocalDate.of(1900, 1, 1))
                .gender("Chưa xác định")
                .build();

        User user = User.builder()
                .userId(generateUserId())
                .username(username)
                .password(encoded)
                .role("READER")
                .status(true)
                .createdAt(LocalDateTime.now())
                .userDetail(userDetail)
                .build();

        userDetail.setUser(user);
        userRepository.save(user);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private String generateUserId() {
        String id;
        do {
            id = generateRandomId(5);
        } while (userRepository.existsById(id));
        return id;
    }

    private String generateRandomId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
