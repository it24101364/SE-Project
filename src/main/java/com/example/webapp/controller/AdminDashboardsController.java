package com.example.webapp.controller;

import com.example.webapp.model.Admin;
import com.example.webapp.repository.AdminRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminDashboardsController {

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminDashboardsController(AdminRepository adminRepo,
                                     PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------- MAIN DASHBOARD ONLY ----------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER', 'STOCK_MANAGER', 'SALES_MANAGER', 'ORDER_MANAGER', 'MARKETING_MANAGER')")
    public String dashboard() {
        return "admin-dashboard";
    }

    // ---------------- BASIC REDIRECT ENDPOINTS ONLY ----------------
    @GetMapping("/feedback")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')")
    public String feedback() {
        return "feedback-dashboard";
    }

    // REMOVED: /admin/users mapping to avoid conflict with UserManagementController

    // ---------------- SUPER ADMIN - Manage Sub Admins ----------------
    @GetMapping("/manage-admins")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String manageAdmins(Model model) {
        List<Admin> admins = adminRepo.findAll();
        model.addAttribute("admins", admins);
        return "manage-admins";
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String createAdmin(@RequestParam String email,
                              @RequestParam String password,
                              @RequestParam(required = false) List<String> roles,
                              RedirectAttributes redirectAttributes) {

        // Check if admin already exists
        if (adminRepo.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "Admin with this email already exists!");
            return "redirect:/admin/manage-admins";
        }

        try {
            Admin admin = new Admin();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));

            // Ensure at least one role is assigned
            if (roles == null || roles.isEmpty()) {
                roles = List.of("USER_MANAGER"); // Default role
            }
            admin.setRoles(Set.copyOf(roles));

            adminRepo.save(admin);
            redirectAttributes.addFlashAttribute("success", "Admin created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating admin: " + e.getMessage());
        }

        return "redirect:/admin/manage-admins";
    }

    @PostMapping("/update-admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String updateAdmin(@RequestParam Long id,
                              @RequestParam(required = false) List<String> roles,
                              RedirectAttributes redirectAttributes) {

        try {
            Admin admin = adminRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));

            // Ensure at least one role is assigned
            if (roles == null || roles.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "At least one role must be assigned!");
                return "redirect:/admin/manage-admins";
            }

            admin.setRoles(Set.copyOf(roles));
            adminRepo.save(admin);

            redirectAttributes.addFlashAttribute("success", "Admin roles updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating admin: " + e.getMessage());
        }

        return "redirect:/admin/manage-admins";
    }

    @PostMapping("/delete-admin/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Prevent deleting yourself (you might want to add this logic)
            Admin currentAdmin = adminRepo.findById(id).orElse(null);
            if (currentAdmin != null) {
                adminRepo.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Admin deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Admin not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting admin: " + e.getMessage());
        }

        return "redirect:/admin/manage-admins";
    }
}