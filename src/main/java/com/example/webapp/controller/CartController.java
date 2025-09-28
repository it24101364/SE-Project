package com.example.webapp.controller;

import com.example.webapp.model.CartItem;
import com.example.webapp.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String userEmail = principal.getName();
        List<CartItem> cartItems = cartService.getCartItems(userEmail);
        double total = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart";
    }


    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String userEmail = principal.getName();
        cartService.addToCart(userEmail, productId);
        return "redirect:/products"; // back to products page
    }


    @PostMapping("/update/{cartId}")
    public String updateQuantity(@PathVariable Long cartId, @RequestParam int quantity) {
        cartService.updateQuantity(cartId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartId}")
    public String removeItem(@PathVariable Long cartId) {
        cartService.removeItem(cartId);
        return "redirect:/cart";
    }

}

