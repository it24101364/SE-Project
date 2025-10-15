package com.example.webapp.controller;

import com.example.webapp.model.Order;
import com.example.webapp.model.PaymentForm;
import com.example.webapp.service.OrderService;
import com.example.webapp.service.OrderEmailService;
import com.example.webapp.strategy.CardPaymentStrategy;
import com.example.webapp.strategy.CashOnDeliveryStrategy;
import com.example.webapp.strategy.PaymentContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final OrderService orderService;
    private final OrderEmailService orderEmailService;

    public PaymentController(OrderService orderService, OrderEmailService orderEmailService) {
        this.orderService = orderService;
        this.orderEmailService = orderEmailService;
    }

    // --- Show payment page ---
    @GetMapping("/{orderId}")
    public String showPaymentPage(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            model.addAttribute("error", "Order not found!");
            return "redirect:/cart"; // or some error page
        }

        model.addAttribute("order", order);
        model.addAttribute("paymentForm", new PaymentForm());
        return "payment";
    }

    // --- Process payment ---
    @PostMapping
    public String processPayment(@ModelAttribute PaymentForm paymentForm, Model model) {
        Order order = orderService.getOrderById(paymentForm.getOrderId());
        if (order == null) {
            model.addAttribute("error", "Order not found!");
            return "payment";
        }

        PaymentContext context = new PaymentContext();
        String message;

        if ("card".equalsIgnoreCase(paymentForm.getPaymentMethod())) {

            // Combine month and year to a string like MM/YY
            String expiryDate = String.format("%02d/%d",
                    paymentForm.getExpiryMonth(),
                    paymentForm.getExpiryYear() % 100); // last 2 digits of year

            context.setPaymentStrategy(new CardPaymentStrategy(
                    paymentForm.getCardNumber(),
                    paymentForm.getCardHolderName(),
                    expiryDate,
                    paymentForm.getCvv()
            ));
            order.setPaymentType("Card");
            order.setPaid(true);
        }
        else if ("cod".equalsIgnoreCase(paymentForm.getPaymentMethod())) {
            context.setPaymentStrategy(new CashOnDeliveryStrategy());
            order.setPaymentType("Cash on Delivery");
            order.setPaid(false); // COD is not yet paid
        } else {
            model.addAttribute("error", "Invalid payment method!");
            model.addAttribute("order", order);
            return "payment";
        }

        message = context.executePayment(order.getTotalAmount());
        orderService.saveOrder(order);
        orderEmailService.sendOrderSummary(order, message);


        model.addAttribute("order", order);
        model.addAttribute("message", message);
        return "payment-success";
    }

    private String detectCardType(String number) {
        if (number == null) return "Unknown";
        number = number.replaceAll("\\s+", "");
        if (number.startsWith("4")) return "Visa";
        if (number.matches("^5[1-5].*")) return "MasterCard";
        if (number.matches("^3[47].*")) return "American Express";
        if (number.matches("^6(?:011|5).*")) return "Discover";
        return "Unknown";
    }
}
