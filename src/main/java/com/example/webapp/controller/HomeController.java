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
            String email = principal.getName();
            user = userService.findByEmail(email);
            cartItemCount = cartService.getCartItemCount(email);
        }

        model.addAttribute("user", user);
        model.addAttribute("cartItemCount", cartItemCount);

        // Featured products - get latest 6 products
        List<Product> featuredProducts = productRepository.findLatestProducts(6);
        model.addAttribute("featuredProducts", featuredProducts);

        return "index";
    }
}