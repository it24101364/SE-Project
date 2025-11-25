package com.example.webapp.controller;

import com.example.webapp.model.Order;
import com.example.webapp.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class OrderManagementController {

    private final OrderService orderService;

    public OrderManagementController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ---------------- ORDER DASHBOARD ----------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ORDER_MANAGER')")
    public String dashboard(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order-dashboard";
    }

    // ---------------- MARK ORDER AS PAID ----------------
    @PostMapping("/mark-paid/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ORDER_MANAGER')")
    public String markPaid(@PathVariable Long id) {
        orderService.markAsPaid(id);
        return "redirect:/admin/orders/dashboard";
    }

    // ---------------- UPDATE ORDER STATUS ----------------
    @PostMapping("/update-status/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ORDER_MANAGER')")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String trackingNumber) {
        orderService.updateStatus(id, status, trackingNumber);
        return "redirect:/admin/orders/dashboard";
    }
}