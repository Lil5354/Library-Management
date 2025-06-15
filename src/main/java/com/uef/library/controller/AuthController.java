package com.uef.library.controller;

import com.uef.library.model.User;
import com.uef.library.service.AuthService;
import com.uef.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Helper method để kiểm tra người dùng đã đăng nhập hay chưa (Giữ nguyên)
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // === BẮT ĐẦU THAY ĐỔI ===
        // Nếu người dùng đã đăng nhập, chuyển hướng về trang chủ
        if (isAuthenticated()) {
            return "redirect:/";
        }
        // === KẾT THÚC THAY ĐỔI ===

        model.addAttribute("user", new User());
        return "auth/register";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "error", required = false) String error,
                            Model model) {
        // === BẮT ĐẦU THAY ĐỔI ===
        // Nếu người dùng đã đăng nhập, chuyển hướng về trang chủ
        if (isAuthenticated()) {
            // Thay vì trang chủ, bạn cũng có thể chuyển hướng đến dashboard cụ thể theo vai trò
            // Ví dụ: return "redirect:/reader/home";
            return "redirect:/";
        }
        // === KẾT THÚC THAY ĐỔI ===

        if (logout != null) {
            model.addAttribute("success", "Bạn đã đăng xuất thành công.");
        }
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        return "auth/login";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String rawPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        // (Giữ nguyên logic xử lý đăng ký)
        if (!rawPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không khớp.");
            return "redirect:/auth/register";
        }

        if (authService.usernameExists(username.trim())) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại.");
            return "redirect:/auth/register";
        }

        authService.registerUser(username.trim(), rawPassword);

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...");
        return "redirect:/auth/login?register_success"; // Nên chuyển về trang login sau khi đăng ký thành công
    }

    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return authService.usernameExists(username);
    }
}