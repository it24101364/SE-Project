package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private final UserRepository userRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, User> pendingUsers = new HashMap<>();

    public UserController(UserRepository userRepo, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Login page
    @GetMapping("/login")  // full path = /user/login
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";  // login.html
    }

    // Registration page
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            if (userRepo.findByEmail(user.getEmail()) != null) {
                model.addAttribute("error", "Email already registered!");
                return "register";
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Generate OTP
            String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
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

    // OTP verification
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
}
