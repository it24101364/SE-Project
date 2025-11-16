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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/products")
    public String showProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model,
            Principal principal) {

        // Get all products
        List<Product> products = productService.getAllProducts();

        // Get featured products for carousel (random 6 products)
        List<Product> featuredProducts = products.stream()
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("featuredProducts", featuredProducts);

        // Filter by category
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }

        // Filter by search query
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Sort by price
        if ("low-high".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice));
        } else if ("high-low".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice).reversed());
        }

        model.addAttribute("products", products != null ? products : new ArrayList<>());

        // Get all unique categories
        List<String> categories = productService.getAllCategories();
        model.addAttribute("categories", categories);

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
