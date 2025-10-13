package com.example.webapp.controller;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderItem;
import com.example.webapp.model.PaymentForm;
import com.example.webapp.model.ShippingForm;
import com.example.webapp.model.CartItem;
import com.example.webapp.model.Product;
import com.example.webapp.service.CartService;
import com.example.webapp.service.OrderItemService;
import com.example.webapp.service.OrderService;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService; // ✅ NEW

    public CheckoutController(CartService cartService,
                              UserService userService,
                              OrderService orderService,
                              OrderItemService orderItemService,
                              ProductService productService) { // ✅ include in constructor
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

        model.addAttribute("cartItems", cartItems);

        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

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
                             Principal principal, Model model) {
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
            // Calculate total price
            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            // Save order
            Order order = new Order();
            order.setUserEmail(email);
            order.setFullName(shippingForm.getFullName());
            order.setAddress(shippingForm.getAddress());
            order.setCity(shippingForm.getCity());
            order.setPostalCode(shippingForm.getPostalCode());
            order.setCountry(shippingForm.getCountry());
            order.setPhone(shippingForm.getPhone());
            order.setTotalAmount(totalPrice);
            orderService.saveOrder(order);

            // Save each order item and reduce stock
            for (CartItem item : cartItems) {
                Product product = item.getProduct();

                // ✅ Reduce product stock count safely
                int newStock = Math.max(product.getStockCount() - item.getQuantity(), 0);
                product.setStockCount(newStock);
                productService.saveProduct(product);

                // ✅ Save order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItemService.saveOrderItem(orderItem);
            }

            // ✅ Clear the user's cart
            cartService.clearCart(email);

            // ✅ Prepare payment info
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
