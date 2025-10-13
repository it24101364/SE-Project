package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.service.StockService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Controller
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // GET mapping for stock dashboard with optional filter
    @GetMapping("/stock/dashboard")
    public String stockDashboard(Model model,
                                 @RequestParam(required = false, defaultValue = "all") String filter) {

        List<Product> products;
        if ("low".equals(filter)) {
            products = stockService.getLowStockProducts();
        } else {
            products = stockService.getAllProducts();
        }

        long lowStockCount = stockService.getLowStockProducts().size();
        int totalStock = products.stream().mapToInt(Product::getStockCount).sum();

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("filter", filter);

        return "stock-dashboard";
    }

    // POST mapping to add new product
    @PostMapping("/stock/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam int stockCount) {
        stockService.addProduct(name, stockCount);
        return "redirect:/stock/dashboard";
    }

    // POST mapping to delete product
    @PostMapping("/stock/delete")
    public String deleteProduct(@RequestParam Long productId) {
        stockService.deleteProduct(productId);
        return "redirect:/stock/dashboard";
    }

    // POST mapping to restock (add quantity to current stock)
    @PostMapping("/stock/restock")
    public String restockProduct(@RequestParam Long productId,
                                 @RequestParam int addStock) {
        stockService.restockProduct(productId, addStock);
        return "redirect:/stock/dashboard";
    }

    @GetMapping("/stock/export")
    public ResponseEntity<String> exportStockCsv(@RequestParam(required = false, defaultValue = "all") String filter) {
        List<Product> products = stockService.filterProducts(filter);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Name,Stock Count,Price,Description"); // CSV header

        for (Product p : products) {
            pw.printf("%d,%s,%d,%.2f,%s%n",
                    p.getId(),
                    p.getName(),
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
