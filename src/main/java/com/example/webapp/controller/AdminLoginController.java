package com.example.webapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminLoginController {

    // Default admin credentials
    private static final String DEFAULT_ADMIN_EMAIL = "salesadmin@example.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    @GetMapping("/admin-login")
    public String adminLoginForm(HttpSession session) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("adminEmail") != null) {
            return "redirect:/sales/dashboard";
        }
        return "admin-login";
    }

    @PostMapping("/admin-login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {

        // Check against default admin credentials
        if (DEFAULT_ADMIN_EMAIL.equals(email) && DEFAULT_ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminEmail", email);
            session.setAttribute("adminRole", "SALES_ADMIN");
            return "redirect:/sales/dashboard";
        }

        model.addAttribute("error", "Invalid email or password");
        return "admin-login";
    }

    @GetMapping("/admin-logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("adminEmail");
        session.removeAttribute("adminRole");
        return "redirect:/admin-login";
    }
}