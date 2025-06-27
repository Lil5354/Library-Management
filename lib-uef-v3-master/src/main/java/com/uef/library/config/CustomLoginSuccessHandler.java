package com.uef.library.config;

import com.uef.library.model.User;
import com.uef.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    // Tiêm UserService vào để lấy thông tin người dùng
    @Autowired
    private UserService userService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectURL = request.getContextPath();
        String username = authentication.getName();

        // === LOGIC MỚI: KIỂM TRA LẦN ĐẦU ĐĂNG NHẬP ===
        // Chỉ kiểm tra với vai trò READER
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_READER"))) {
            User user = userService.findByUsername(username).orElse(null);

            // Nếu user tồn tại và fullName chưa được cập nhật
            if (user != null && user.getUserDetail() != null &&
                    (user.getUserDetail().getFullName() == null || "Chưa cập nhật".equalsIgnoreCase(user.getUserDetail().getFullName()))) {

                // Đặt cờ hiệu vào session để báo cho frontend biết cần hiển thị popup
                request.getSession().setAttribute("showFirstLoginPopup", true);
            }
        }
        // ===========================================

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            switch (role) {
                case "ROLE_ADMIN":
                    redirectURL += "/admin/dashboard";
                    break;
                case "ROLE_STAFF":
                    redirectURL += "/staff/home";
                    break;
                case "ROLE_READER":
                    redirectURL += "/"; // Reader sẽ được chuyển về trang chủ
                    break;
                default:
                    redirectURL += "/access-denied";
                    break;
            }
            break;
        }
        request.getSession().setAttribute("username", username);
        response.sendRedirect(redirectURL);
    }
}