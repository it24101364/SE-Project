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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    /**
     * Home page mapping
     */
    @GetMapping("/")  // Home page
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            user = userService.findByUsername(username);
        }

        model.addAttribute("user", user);

        int cartItemCount = 0;
        if (user != null) {
            cartItemCount = cartService.getCartItemCount(user.getEmail());
        }
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
