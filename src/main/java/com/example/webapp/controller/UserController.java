package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {

    private final UserRepository userRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    // In-memory storage for OTPs and tokens
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, User> pendingUsers = new HashMap<>();
    private final Map<String, PasswordResetToken> passwordResetTokens = new HashMap<>();

    public UserController(UserRepository userRepo, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------- LOGIN -------------------
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // ------------------- REGISTER -------------------
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        boolean hasError = false;

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("usernameError", "Username is required!");
            hasError = true;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("emailError", "Email is required!");
            hasError = true;
        } else if (userRepo.findByEmail(user.getEmail()) != null) {
            model.addAttribute("emailError", "Email is already registered!");
            hasError = true;
        }

        if (user.getPassword() == null || user.getPassword().length() < 8) {
            model.addAttribute("passwordError", "Password must be at least 8 characters!");
            hasError = true;
        }

        if (hasError) return "register";

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Generate OTP
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
            otpStorage.put(user.getEmail(), otp);
            pendingUsers.put(user.getEmail(), user);

            mailService.sendOtp(user.getEmail(), otp);

            model.addAttribute("email", user.getEmail());
            return "verify-otp";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "register";
        }
    }

    // ------------------- OTP VERIFICATION -------------------
    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            Model model) {
        String correctOtp = otpStorage.get(email);
        if (correctOtp != null && correctOtp.equals(otp)) {
            User newUser = pendingUsers.get(email);
            userRepo.save(newUser);

            otpStorage.remove(email);
            pendingUsers.remove(email);

            return "redirect:/login";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Invalid OTP! Try again.");
        return "verify-otp";
    }

    // ------------------- FORGOT PASSWORD -------------------
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Email not registered!");
            return "forgot-password";
        }

        // Generate password reset token
        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, new PasswordResetToken(email, LocalDateTime.now().plusMinutes(30)));

        // Send reset email
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        mailService.sendPasswordResetLink(email, resetLink);

        model.addAttribute("message", "Password reset link sent to your email.");
        return "forgot-password";
    }

    // ------------------- RESET PASSWORD -------------------
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        PasswordResetToken prt = passwordResetTokens.get(token);
        if (prt == null || prt.getExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired password reset token!");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {
        PasswordResetToken prt = passwordResetTokens.get(token);
        if (prt == null || prt.getExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token!");
            return "reset-password";
        }

        if (password == null || password.length() < 8 || !password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("passwordError", "Passwords must match and be at least 8 characters!");
            return "reset-password";
        }

        User user = userRepo.findByEmail(prt.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);

        passwordResetTokens.remove(token);

        model.addAttribute("message", "Password reset successfully! You can now login.");
        return "redirect:/login";
    }

    // ------------------- Inner class for token -------------------
    private static class PasswordResetToken {
        private final String email;
        private final LocalDateTime expiry;

        public PasswordResetToken(String email, LocalDateTime expiry) {
            this.email = email;
            this.expiry = expiry;
        }

        public String getEmail() { return email; }
        public LocalDateTime getExpiry() { return expiry; }
    }
}
