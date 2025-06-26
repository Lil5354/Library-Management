package com.uef.library.config;

import com.uef.library.model.User;
import com.uef.library.model.UserDetail;
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

        User user = userService.findByUsername(username).orElse(null);

        if (user != null && "READER".equals(user.getRole())) {
            UserDetail userDetail = user.getUserDetail();
            // Điều kiện mới: Kiểm tra email rỗng hoặc có giá trị mặc định
            if (userDetail != null && (!StringUtils.hasText(userDetail.getEmail()) || "Chưa cập nhật".equalsIgnoreCase(userDetail.getEmail()))) {
                request.getSession().setAttribute("showFirstLoginPopup", true);
            } else {
                request.getSession().removeAttribute("showFirstLoginPopup");
            }
        }

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
                    redirectURL += "/";
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