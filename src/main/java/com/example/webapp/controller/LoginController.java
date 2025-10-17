//package com.example.webapp.controller;
//
//
//import org.springframework.ui.Model;
//import org.springframework.stereotype.Controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import com.example.webapp.model.User;
//
//@Controller
//public class LoginController {
//
//    @GetMapping("/login")
//    public String showLoginForm(Model model) {
//        model.addAttribute("user", new User());  // <-- add this
//        return "login";
//    }
//
//    @PostMapping("/login")
//    public String processLogin(@ModelAttribute("user") User user) {
//        // handle login
//        return "redirect:/dashboard";
//    }
//}
