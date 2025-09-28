package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
    public String showAllProducts(Model model) {
        List<Product> products = productService.getAllProducts(); // fetch from DB
        model.addAttribute("products", products);
        return "products"; // Thymeleaf page: products.html
    }


}
