package com.example.webapp.controller;

import com.example.webapp.model.Order;
import com.example.webapp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderManagementController {

    private final OrderService orderService;

    public OrderManagementController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Show dashboard
    @GetMapping("/dashboard")  // <-- change here
    public String dashboard(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order-dashboard";  // make sure your template is at templates/order-dashboard.html
    }

    @PostMapping("/mark-paid/{id}")
    public String markPaid(@PathVariable("id") Long id) {
        orderService.markAsPaid(id);
        return "redirect:/orders/dashboard";
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String trackingNumber) {
        orderService.updateStatus(id, status, trackingNumber);
        return "redirect:/orders/dashboard";
    }

}
