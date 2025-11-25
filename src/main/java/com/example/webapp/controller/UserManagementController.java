package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserRepository userRepo;

    public UserManagementController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ---------------- LIST USERS ----------------
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String viewUsers(Model model,
                            @RequestParam(required = false) String search) {

        List<User> users = (search != null && !search.isEmpty())
                ? userRepo.searchUsers(search.toLowerCase())
                : userRepo.findAll();

        model.addAttribute("users", users);
        model.addAttribute("search", search);

        return "user-dashboard"; // Make sure this matches your HTML file name
    }

    // ---------------- DELETE USER ----------------
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}