package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.service.StockService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // ---------------- STOCK DASHBOARD ----------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'STOCK_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String stockDashboard(Model model,
                                 @RequestParam(value = "filter", defaultValue = "all") String filter,
                                 @RequestParam(value = "category", defaultValue = "") String category) {

        List<Product> products = stockService.getAllProducts();

        // Apply filters
        if ("low".equals(filter)) {
            products = products.stream()
                    .filter(p -> p.getStockCount() != null && p.getStockCount() < 10)
                    .collect(Collectors.toList());
        }

        if (!category.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        // Calculate statistics
        long lowStockCount = stockService.getAllProducts().stream()
                .filter(p -> p.getStockCount() != null && p.getStockCount() < 10)
                .count();

        int totalStock = products.stream()
                .filter(p -> p.getStockCount() != null)
                .mapToInt(Product::getStockCount)
                .sum();

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("filter", filter);
        model.addAttribute("selectedCategory", category);

        return "stock-dashboard";
    }

    // ---------------- ADD PRODUCT ----------------
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'STOCK_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String addProduct(@RequestParam String name,
                             @RequestParam String category,
                             @RequestParam Integer stockCount) {
        stockService.addProduct(name, category, stockCount);
        return "redirect:/admin/stock/dashboard";
    }

    // ---------------- DELETE PRODUCT ----------------
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'STOCK_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String deleteProduct(@RequestParam Long productId) {
        stockService.deleteProduct(productId);
        return "redirect:/admin/stock/dashboard";
    }

    // ---------------- RESTOCK PRODUCT ----------------
    @PostMapping("/restock")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'STOCK_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String restockProduct(@RequestParam Long productId,
                                 @RequestParam Integer addStock) {
        stockService.restockProduct(productId, addStock);
        return "redirect:/admin/stock/dashboard";
    }

    // ---------------- EXPORT CSV ----------------
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'STOCK_MANAGER')") // FIXED: Added SUPER_ADMIN
    public ResponseEntity<String> exportStockCsv(@RequestParam(required = false, defaultValue = "all") String filter,
                                                 @RequestParam(required = false, defaultValue = "") String category) {

        List<Product> products = stockService.getAllProducts();

        // Apply same filters as dashboard
        if ("low".equals(filter)) {
            products = products.stream()
                    .filter(p -> p.getStockCount() != null && p.getStockCount() < 10)
                    .collect(Collectors.toList());
        }

        if (!category.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // CSV Header
        pw.println("ID,Name,Category,Stock Count,Price,Description,Created Date");

        // CSV Data
        for (Product product : products) {
            pw.printf("%d,%s,%s,%d,%.2f,%s,%s%n",
                    product.getId(),
                    escapeCsv(product.getName()),
                    escapeCsv(product.getCategory() != null ? product.getCategory() : "N/A"),
                    product.getStockCount() != null ? product.getStockCount() : 0,
                    product.getPrice() != null ? product.getPrice() : 0.0,
                    escapeCsv(product.getDescription() != null ? product.getDescription() : ""),
                    formatDate(product.getCreatedAt())
            );
        }

        pw.flush();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(sw.toString());
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }

    private String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}