package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserRepository userRepo;

    public UserManagementController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ---------------- LIST USERS ----------------
    @GetMapping("/users")
    public String viewUsers(Model model,
                            HttpSession session,
                            @RequestParam(required = false) String search) {

        // Check if admin is logged in
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        List<User> users;

        if (search != null && !search.isEmpty()) {
            users = userRepo.searchUsers(search.toLowerCase());
            model.addAttribute("search", search);
        } else {
            users = userRepo.findAll();
        }

        model.addAttribute("users", users);

        // NO SUBFOLDER — return template directly
        return "user-dashboard";
    }

    // ---------------- DELETE USER ----------------
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {

        if (session.getAttribute("adminEmail") == null)
            return "redirect:/admin-login";

        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}
