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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        // Chart data
        model.addAttribute("productNames", saleService.getTopSellingProductsNames());
        model.addAttribute("productSalesAmounts", saleService.getTopSellingProductsAmounts());
        model.addAttribute("months", saleService.getMonthlySalesMonths());
        model.addAttribute("monthlyAmounts", saleService.getMonthlySalesAmounts());


        return "sales-dashboard";
    }

    @GetMapping
    public String getSales(HttpSession session, Model model) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        List<Sale> sales = saleService.getAllSales();
        model.addAttribute("sales", sales);

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

        model.addAttribute("sales", saleService.getSalesByDate(start, end));
        model.addAttribute("filteredTotal", saleService.getTotalSalesByDate(start, end));

        addAnalyticsData(model);

        return "sales-dashboard";
    }

    @GetMapping("/report")
    public String generateReport(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 HttpSession session,
                                 Model model) {
        if (session.getAttribute("adminEmail") == null) {
            return "redirect:/admin-login";
        }

        List<Sale> sales;
        double totalAmount;
        String reportRange = "All Time";

        if (startDate != null && endDate != null) {
            LocalDate startLocalDate = LocalDate.parse(startDate.replace(",", ""));
            LocalDate endLocalDate = LocalDate.parse(endDate.replace(",", ""));

            LocalDateTime start = startLocalDate.atStartOfDay();
            LocalDateTime end = endLocalDate.atTime(23, 59, 59);

            sales = saleService.getSalesByDate(start, end);
            totalAmount = saleService.getTotalSalesByDate(start, end);
        } else {
            sales = saleService.getAllSales();
            totalAmount = saleService.getTotalSales();
        }

        model.addAttribute("sales", sales);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("reportDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        model.addAttribute("reportRange", reportRange); // ✅ add this
        return "sales-report";
    }

    // ✅ PDF DOWNLOAD ENDPOINT
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
        String reportRange = "All Time"; // ✅ Initialize

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate startLocalDate = LocalDate.parse(startDate.replace(",", ""));
            LocalDate endLocalDate = LocalDate.parse(endDate.replace(",", ""));

            LocalDateTime start = startLocalDate.atStartOfDay();
            LocalDateTime end = endLocalDate.atTime(23, 59, 59);

            sales = saleService.getSalesByDate(start, end);
            totalAmount = saleService.getTotalSalesByDate(start, end);

            reportRange = "From " + startLocalDate + " to " + endLocalDate; // ✅ set range
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
