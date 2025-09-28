package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.service.CartService;
import com.example.webapp.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {
    private final ProductService productService;
    private final CartService cartService; // this must exist

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;  // now it works
    }


    // Show all products
    @GetMapping("/welcome")
    public String showWelcomePage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "welcome"; // welcome.html
    }

    // Show Add Product Form
    @GetMapping("/add-product")
    public String showAddProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "add-product"; // add-product.html
    }

    // Handle form submission
    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute Product product, Model model) {
        try {
            productService.saveProduct(product);
            model.addAttribute("successMessage", "Product added successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding product: " + e.getMessage());
        }
        model.addAttribute("product", new Product()); // clear form
        return "add-product"; // stay on the same page
    }
    @GetMapping("/products")
    public String showAllProducts(Model model, Principal principal) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        int cartItemCount = 0;
        if (principal != null) {
            String userEmail = principal.getName();
            cartItemCount = cartService.getCartItems(userEmail).size();
        }
        model.addAttribute("cartItemCount", cartItemCount);

        return "products";
    }





}
