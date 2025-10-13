package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import com.example.webapp.service.CartService;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;

    public ProductController(ProductService productService, CartService cartService, UserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
    }

    // Show all products page
    @GetMapping("/products")
    public String showProducts(Model model, Principal principal) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        int cartItemCount = 0;
        User user = null;

        if (principal != null) {
            String email = principal.getName();
            var items = cartService.getCartItems(email);
            cartItemCount = (items != null) ? items.size() : 0;
            user = userService.findByEmail(email);
        }

        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("user", user);

        return "products";
    }

    // Show add product page
    @GetMapping("/product/add")
    public String showAddProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "add-product"; // Thymeleaf template
    }

    // Handle form submission
    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute Product product, Model model) {
        try {
            productService.saveProduct(product);
            model.addAttribute("successMessage", "Product added successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding product: " + e.getMessage());
        }

        model.addAttribute("product", new Product()); // clear form
        return "add-product";
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        try {
            productService.deleteProduct(id);
            model.addAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }

        model.addAttribute("products", productService.getAllProducts());
        return "redirect:/stock-dashboard";
    }

}
