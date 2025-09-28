package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.model.ShippingForm;
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
    private final CartService cartService; // this must exist
    private final UserService userService;


    public ProductController(ProductService productService, CartService cartService,UserService userService) {
        this.productService = productService;
        this.cartService = cartService;  // now it works
        this.userService = userService;


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
    public String showProducts(Model model, Principal principal) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        int cartItemCount = 0;
        User user = null;

        if (principal != null) {
            String email = principal.getName(); // email from logged-in user
            var items = cartService.getCartItems(email);
            cartItemCount = (items != null) ? items.size() : 0;
            user = userService.findByEmail(email); // use email instead of username
            model.addAttribute("user", user);
        }


        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("user", user);

        return "products";
    }





}
