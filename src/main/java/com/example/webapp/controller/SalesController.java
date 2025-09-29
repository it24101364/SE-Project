package com.example.webapp.controller;

import com.example.webapp.service.SaleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/sales")
public class SalesController {

    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("sales", saleService.getAllSales());
        model.addAttribute("totalSales", saleService.getTotalSales());
        return "sales-dashboard";
    }

    @GetMapping("/filter")
    public String filter(@RequestParam String startDate,
                         @RequestParam String endDate,
                         Model model) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        model.addAttribute("sales", saleService.getSalesByDate(start, end));
        model.addAttribute("totalSales", saleService.getTotalSales());
        return "sales-dashboard";
    }
    @GetMapping("/sales-dashboard")
    public String salesDashboard(HttpSession session, Model model) {
        Object email = session.getAttribute("email");
        if (!"salesadmin@example.com".equals(email)) {
            return "redirect:/login"; // Not allowed
        }
        model.addAttribute("sales", "All sales data...");
        return "sales-dashboard";
    }

}
