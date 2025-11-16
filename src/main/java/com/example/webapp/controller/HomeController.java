package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.service.CartService;
import com.example.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @GetMapping("/")  // Home page
    public String index(Model model, Principal principal) {
        User user = null;
        int cartItemCount = 0;

        if (principal != null) {
            String email = principal.getName(); // this is the logged-in email
            user = userService.findByEmail(email); // fetch user by email
            cartItemCount = cartService.getCartItemCount(email);
        }

        model.addAttribute("user", user);
        model.addAttribute("cartItemCount", cartItemCount);

        // Featured products (top 6)
        List<Product> featuredProducts = productRepository.findFeaturedProducts(6);
        if (featuredProducts == null || featuredProducts.isEmpty()) {
            Pageable pageable = PageRequest.of(0, 6);
            Page<Product> products = productRepository.findAll(pageable);
            featuredProducts = products.getContent();
        }
        model.addAttribute("featuredProducts", featuredProducts);

        return "index";
    }
}