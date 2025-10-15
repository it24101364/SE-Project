package com.example.webapp.controller;

import com.example.webapp.model.*;
import com.example.webapp.service.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    public CheckoutController(CartService cartService,
                              UserService userService,
                              OrderService orderService,
                              OrderItemService orderItemService,
                              ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    // ------------------- SHOW CHECKOUT PAGE -------------------
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        List<CartItem> cartItems = cartService.getCartItems(email);

        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("cartEmpty", true);
            return "checkout";
        }

        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("shipping", new ShippingForm());
        model.addAttribute("user", userService.findByEmail(email));

        return "checkout";
    }

    // ------------------- PLACE ORDER -------------------
    @PostMapping("/checkout")
    @Transactional
    public String placeOrder(@Valid @ModelAttribute("shipping") ShippingForm shippingForm,
                             BindingResult bindingResult,
                             Principal principal,
                             Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        List<CartItem> cartItems = cartService.getCartItems(email);

        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty!");
            return "checkout";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartItems", cartItems);
            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
            model.addAttribute("totalPrice", totalPrice);
            return "checkout";
        }

        try {
            // ------------------- CREATE ORDER -------------------
            Order order = new Order();
            order.setUserEmail(email);
            order.setFullName(shippingForm.getFullName());
            order.setAddress(shippingForm.getAddress());
            order.setCity(shippingForm.getCity());
            order.setPostalCode(shippingForm.getPostalCode());
            order.setCountry(shippingForm.getCountry());
            order.setPhone(shippingForm.getPhone());

            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
            order.setTotalAmount(totalPrice);

            // Initialize order items list
            List<OrderItem> orderItems = new ArrayList<>();
            order.setItems(orderItems);

            // ------------------- CREATE ORDER ITEMS -------------------
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();

                // Reduce stock safely
                int newStock = Math.max(product.getStockCount() - cartItem.getQuantity(), 0);
                product.setStockCount(newStock);
                productService.saveProduct(product);

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(product.getPrice());

                // Add to order's list
                orderItems.add(orderItem);
            }

            // ------------------- SAVE ORDER & ORDER ITEMS -------------------
            orderService.saveOrder(order); // CascadeType.ALL will save orderItems

            // ------------------- CLEAR CART -------------------
            cartService.clearCart(email);

            // ------------------- PREPARE PAYMENT -------------------
            PaymentForm paymentForm = new PaymentForm();
            paymentForm.setOrderId(order.getId());

            model.addAttribute("order", order);
            model.addAttribute("paymentForm", paymentForm);
            model.addAttribute("user", userService.findByEmail(email));

            return "payment";

        } catch (Exception e) {
            logger.error("Error placing order for user {}: {}", email, e.getMessage(), e);
            model.addAttribute("error", "Something went wrong while placing your order. Please try again.");
            return "checkout";
        }
    }
}
