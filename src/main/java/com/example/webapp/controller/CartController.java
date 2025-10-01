package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("cartItems", cartService.getCart(user.getId()));
        return "cart";  // Thymeleaf template for cart view
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            cartService.addToCart(user.getId(), productId, quantity);
            return "redirect:/cart";
        } catch (Exception e) {
            return "redirect:/products?error=" + e.getMessage();  // Assumes product catalog redirects here
        }
    }

    @GetMapping("/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            cartService.removeFromCart(user.getId(), productId);
            return "redirect:/cart";
        } catch (Exception e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
    }
}