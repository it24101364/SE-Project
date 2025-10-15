package com.example.webapp.controller;

import com.example.webapp.model.CartItem;
import com.example.webapp.model.User;
import com.example.webapp.service.CartService;
import com.example.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String showCart(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String userEmail = principal.getName();
        User user = userService.findByEmail(userEmail); // ✅ get user
        List<CartItem> cartItems = cartService.getCartItems(userEmail);
        double total = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("user", user); // ✅ add to model
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
    public String updateQuantity(@PathVariable Long cartId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(cartId, quantity);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }



    @PostMapping("/remove/{cartId}")
    public String removeItem(@PathVariable Long cartId) {
        cartService.removeItem(cartId);
        return "redirect:/cart";
    }
    @PostMapping("/buy-now/{productId}")
    public String buyNow(@PathVariable Long productId, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // must be logged in
        }
        String userEmail = principal.getName();
        cartService.buyNow(userEmail, productId);
        return "redirect:/cart"; // go directly to cart page
    }



}

