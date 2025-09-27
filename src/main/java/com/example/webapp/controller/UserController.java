package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.MailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private final UserRepository userRepo;
    private final MailService mailService;

    // Temporary storage for OTPs & pending users
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, User> pendingUsers = new HashMap<>();

    // Password encoder for hashing and verification
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepo, MailService mailService) {
        this.userRepo = userRepo;
        this.mailService = mailService;
    }

    // Index page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Registration form
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Register user and send OTP
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            if (userRepo.findByUsername(user.getUsername()) != null) {
                model.addAttribute("error", "Username already exists!");
                return "register";
            }

            if (userRepo.findByEmail(user.getEmail()) != null) {
                model.addAttribute("error", "Email already registered!");
                return "register";
            }

            // Hash the password before storing temporarily
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Generate OTP
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

            // Store pending user and OTP
            otpStorage.put(user.getEmail(), otp);
            pendingUsers.put(user.getEmail(), user);

            // Send OTP email
            mailService.sendOtp(user.getEmail(), otp);

            // Forward to OTP verification page
            model.addAttribute("email", user.getEmail());
            return "verify-otp";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "register";
        }
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            Model model) {

        String correctOtp = otpStorage.get(email);
        if (correctOtp != null && correctOtp.equals(otp)) {
            User newUser = pendingUsers.get(email);

            // Save verified user into DB
            userRepo.save(newUser);

            // Clear temporary storage
            otpStorage.remove(email);
            pendingUsers.remove(email);

            // Redirect to login page after successful verification
            return "redirect:/login";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Invalid OTP! Try again.");
        return "verify-otp";
    }

    // Login form
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // Login user
    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model) {
        User existingUser = userRepo.findByUsername(user.getUsername());
        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            model.addAttribute("user", existingUser);
            return "welcome";
        }

        model.addAttribute("error", "Invalid username or password!");
        return "login";
    }
}
