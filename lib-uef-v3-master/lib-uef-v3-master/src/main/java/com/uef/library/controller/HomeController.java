package com.uef.library.controller;

import org.springframework.security.access.prepost.PreAuthorize; // Thêm import này
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage() {
        return "home/index";
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public String viewCategories() {
        return "home/categories";
    }


    @GetMapping("/about")
    public String about() {
        return "home/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "home/contact";
    }
}