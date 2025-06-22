package com.uef.library.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Lấy vai trò của người dùng
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectURL = request.getContextPath();

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

            break; // chỉ xử lý role đầu tiên
        }
        request.getSession().setAttribute("username", authentication.getName());
        response.sendRedirect(redirectURL);
    }
}
