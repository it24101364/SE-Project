package com.example.webapp.controller;

import com.example.webapp.model.Order;
import com.example.webapp.model.PaymentForm;
import com.example.webapp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/payment")
    public String processPayment(@ModelAttribute PaymentForm paymentForm, Model model) {

        Order order = orderService.getOrderById(paymentForm.getOrderId());
        if (order == null) {
            model.addAttribute("error", "Order not found!");
            model.addAttribute("order", order);
            return "payment";
        }

        // Detect card type
        String cardType = detectCardType(paymentForm.getCardNumber());

        // Update order payment
        order.setPaymentType(cardType);
        order.setPaid(true);
        orderService.saveOrder(order);

        model.addAttribute("order", order);
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
