package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.service.StockService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // --- STOCK DASHBOARD (with filter + category support) ---
    @GetMapping("/dashboard")
    public String stockDashboard(Model model,
                                 @RequestParam(value = "filter", defaultValue = "all") String filter,
                                 @RequestParam(value = "category", defaultValue = "") String category) {

        List<Product> products = stockService.getAllProducts();

        // Filter by stock level
        if ("low".equals(filter)) {
            products = products.stream()
                    .filter(p -> p.getStockCount() < 5)
                    .collect(Collectors.toList());
        }

        // Filter by category if selected
        if (!category.isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        long lowStockCount = stockService.getLowStockProducts().size();
        int totalStock = products.stream().mapToInt(Product::getStockCount).sum();

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("filter", filter);
        model.addAttribute("selectedCategory", category);

        return "stock-dashboard";
    }

    // --- ADD PRODUCT ---
    @PostMapping("/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam int stockCount) {
        stockService.addProduct(name, stockCount);
        return "redirect:/stock/dashboard";
    }

    // --- DELETE PRODUCT ---
    @PostMapping("/delete")
    public String deleteProduct(@RequestParam Long productId) {
        stockService.deleteProduct(productId);
        return "redirect:/stock/dashboard";
    }

    // --- RESTOCK PRODUCT ---
    @PostMapping("/restock")
    public String restockProduct(@RequestParam Long productId,
                                 @RequestParam int addStock) {
        stockService.restockProduct(productId, addStock);
        return "redirect:/stock/dashboard";
    }

    // --- EXPORT TO CSV ---
    @GetMapping("/export")
    public ResponseEntity<String> exportStockCsv(@RequestParam(required = false, defaultValue = "all") String filter,
                                                 @RequestParam(required = false, defaultValue = "") String category) {
        List<Product> products = stockService.getAllProducts();

        // Apply same filters for export
        if ("low".equals(filter)) {
            products = products.stream()
                    .filter(p -> p.getStockCount() < 5)
                    .collect(Collectors.toList());
        }

        if (!category.isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Name,Category,Stock Count,Price,Description"); // CSV header

        for (Product p : products) {
            pw.printf("%d,%s,%s,%d,%.2f,%s%n",
                    p.getId(),
                    p.getName(),
                    p.getCategory() != null ? p.getCategory() : "",
                    p.getStockCount(),
                    p.getPrice(),
                    p.getDescription() != null ? p.getDescription().replaceAll(",", " ") : ""
            );
        }

        pw.flush();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sw.toString());
    }
}
