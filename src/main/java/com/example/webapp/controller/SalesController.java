package com.example.webapp.controller;

import com.example.webapp.model.Sale;
import com.example.webapp.service.SaleService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/sales")
public class SalesController {

    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    // ---------------- SALES DASHBOARD ----------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String dashboard(Model model) {
        addAnalyticsData(model);
        model.addAttribute("sales", saleService.getAllSales());
        model.addAttribute("filteredTotal", saleService.getTotalSales());

        // Add chart data
        model.addAttribute("productNames", saleService.getTopSellingProductsNames());
        model.addAttribute("productSalesAmounts", saleService.getTopSellingProductsAmounts());
        model.addAttribute("months", saleService.getMonthlySalesMonths());
        model.addAttribute("monthlyAmounts", saleService.getMonthlySalesAmounts());

        return "sales-dashboard";
    }

    // ---------------- FILTER SALES BY DATE ----------------
    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String filter(@RequestParam String startDate,
                         @RequestParam String endDate,
                         Model model) {

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);

        List<Sale> filteredSales = saleService.getSalesByDate(start, end);
        model.addAttribute("sales", filteredSales);
        model.addAttribute("filteredTotal", saleService.getTotalSales(filteredSales));

        // Chart data for filtered results
        model.addAttribute("productNames", saleService.getTopSellingProductsNames(filteredSales));
        model.addAttribute("productSalesAmounts", saleService.getTopSellingProductsAmounts(filteredSales));
        model.addAttribute("months", saleService.getMonthlySalesMonths(filteredSales));
        model.addAttribute("monthlyAmounts", saleService.getMonthlySalesAmounts(filteredSales));

        // Analytics summary
        model.addAttribute("totalSales", saleService.getTotalSales(filteredSales));
        model.addAttribute("totalUnitsSold", saleService.getTotalUnitsSold(filteredSales));
        model.addAttribute("averageOrderValue", saleService.getAverageOrderValue(filteredSales));

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "sales-dashboard";
    }

    // ---------------- UPDATE SALE (GET) ----------------
    @GetMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String updateSaleForm(@PathVariable Long id, Model model) {
        Sale sale = saleService.getSaleById(id);
        model.addAttribute("sale", sale);
        return "sales-update-form";
    }

    // ---------------- UPDATE SALE (POST) ----------------
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String updateSale(@ModelAttribute Sale sale) {
        sale.setTotalAmount(sale.getQuantity() * sale.getPrice());
        saleService.updateSale(sale);
        return "redirect:/admin/sales/dashboard";
    }

    // ---------------- DELETE SALE ----------------
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return "redirect:/admin/sales/dashboard";
    }

    // ---------------- PDF REPORT ----------------
    @GetMapping("/report-pdf")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SALES_MANAGER')") // FIXED: Added SUPER_ADMIN
    public void generatePdfReport(@RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  HttpServletResponse response) {
        try {
            List<Sale> sales;
            double totalAmount;
            String reportRange = "All Time";

            if (startDate != null && !startDate.isEmpty() &&
                    endDate != null && !endDate.isEmpty()) {

                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);

                sales = saleService.getSalesByDate(start, end);
                totalAmount = saleService.getTotalSales(sales);
                reportRange = "From " + startDate + " to " + endDate;
            } else {
                sales = saleService.getAllSales();
                totalAmount = saleService.getTotalSales();
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=sales-report.pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            document.add(new Paragraph("Sales Report"));
            document.add(new Paragraph("Report Period: " + reportRange));
            document.add(new Paragraph("Generated At: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            document.add(new Paragraph("Total Amount: $" + String.format("%.2f", totalAmount)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Order ID");
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Price");
            table.addCell("Total");
            table.addCell("Date");

            for (Sale sale : sales) {
                table.addCell(String.valueOf(sale.getOrderId()));
                table.addCell(sale.getProductName());
                table.addCell(String.valueOf(sale.getQuantity()));
                table.addCell("$" + String.format("%.2f", sale.getPrice()));
                table.addCell("$" + String.format("%.2f", sale.getTotalAmount()));
                table.addCell(sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }

            document.add(table);
            document.close();
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPER ----------------
    private void addAnalyticsData(Model model) {
        model.addAttribute("totalSales", saleService.getTotalSales());
        model.addAttribute("totalUnitsSold", saleService.getTotalUnitsSold());
        model.addAttribute("averageOrderValue", saleService.getAverageOrderValue());
    }
}