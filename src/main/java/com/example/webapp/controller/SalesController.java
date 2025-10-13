package com.example.webapp.controller;

import com.example.webapp.model.Sale;
import com.example.webapp.service.SaleService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SalesController {

    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        addAnalyticsData(model);
        model.addAttribute("sales", saleService.getAllSales());
        model.addAttribute("filteredTotal", saleService.getTotalSales());

        model.addAttribute("productNames", saleService.getTopSellingProductsNames());
        model.addAttribute("productSalesAmounts", saleService.getTopSellingProductsAmounts());
        model.addAttribute("months", saleService.getMonthlySalesMonths());
        model.addAttribute("monthlyAmounts", saleService.getMonthlySalesAmounts());

        return "sales-dashboard";
    }

    @GetMapping("/filter")
    public String filter(@RequestParam String startDate,
                         @RequestParam String endDate,
                         HttpSession session,
                         Model model) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        LocalDate startLocalDate = LocalDate.parse(startDate.replace(",", ""));
        LocalDate endLocalDate = LocalDate.parse(endDate.replace(",", ""));
        LocalDateTime start = startLocalDate.atStartOfDay();
        LocalDateTime end = endLocalDate.atTime(23, 59, 59);

        List<Sale> filteredSales = saleService.getSalesByDate(start, end);
        model.addAttribute("sales", filteredSales);
        model.addAttribute("filteredTotal", saleService.getTotalSales(filteredSales));

        model.addAttribute("productNames", saleService.getTopSellingProductsNames(filteredSales));
        model.addAttribute("productSalesAmounts", saleService.getTopSellingProductsAmounts(filteredSales));
        model.addAttribute("months", saleService.getMonthlySalesMonths(filteredSales));
        model.addAttribute("monthlyAmounts", saleService.getMonthlySalesAmounts(filteredSales));

        model.addAttribute("totalSales", saleService.getTotalSales(filteredSales));
        model.addAttribute("totalUnitsSold", saleService.getTotalUnitsSold(filteredSales));
        model.addAttribute("averageOrderValue", saleService.getAverageOrderValue(filteredSales));

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "sales-dashboard";
    }

    @GetMapping("/update/{id}")
    public String updateSaleForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }
        Sale sale = saleService.getSaleById(id);
        model.addAttribute("sale", sale);
        return "sales-update-form";
    }

    @PostMapping("/update")
    public String updateSale(@ModelAttribute Sale sale, HttpSession session) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        sale.setTotalAmount(sale.getQuantity() * sale.getPrice());
        saleService.updateSale(sale); // ✅ Adjusts stock automatically

        return "redirect:/sales/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        saleService.deleteSale(id); // ✅ Restores product stock automatically

        return "redirect:/sales/dashboard";
    }

    @GetMapping("/report-pdf")
    public void generatePdfReport(@RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  HttpServletResponse response,
                                  HttpSession session) throws Exception {
        if (session.getAttribute("adminEmail") == null) {
            response.sendRedirect("/admin-login");
            return;
        }

        List<Sale> sales;
        double totalAmount;
        String reportRange = "All Time";

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate startLocalDate = LocalDate.parse(startDate.replace(",", ""));
            LocalDate endLocalDate = LocalDate.parse(endDate.replace(",", ""));
            LocalDateTime start = startLocalDate.atStartOfDay();
            LocalDateTime end = endLocalDate.atTime(23, 59, 59);

            sales = saleService.getSalesByDate(start, end);
            totalAmount = saleService.getTotalSalesByDate(start, end);
            reportRange = "From " + startLocalDate + " to " + endLocalDate;
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
        document.add(new Paragraph("Total Amount: $" + totalAmount));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
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
            table.addCell(String.valueOf(sale.getPrice()));
            table.addCell(String.valueOf(sale.getTotalAmount()));
            table.addCell(sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        document.add(table);
        document.close();
    }

    private void addAnalyticsData(Model model) {
        model.addAttribute("totalSales", saleService.getTotalSales());
        model.addAttribute("totalUnitsSold", saleService.getTotalUnitsSold());
        model.addAttribute("averageOrderValue", saleService.getAverageOrderValue());
        model.addAttribute("salesByProduct", saleService.getSalesByProduct());
        model.addAttribute("topSellingProducts", saleService.getTopSellingProducts());
        model.addAttribute("monthlySales", saleService.getMonthlySales());
    }
}
