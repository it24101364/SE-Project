package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderItem;
import com.example.webapp.model.Sale;
import com.example.webapp.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    // ================= Basic Sale Operations =================

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<Sale> getSalesByDate(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    public double getTotalSales() {
        return saleRepository.findAll().stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    public double getTotalSalesByDate(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end).stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    public long getTotalUnitsSold() {
        return saleRepository.findAll().stream()
                .mapToLong(Sale::getQuantity)
                .sum();
    }

    public double getAverageOrderValue() {
        List<Sale> sales = saleRepository.findAll();
        if (sales.isEmpty()) return 0.0;
        return sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .average()
                .orElse(0.0);
    }

    public void addSaleFromOrderItem(OrderItem item, Order order) {
        Sale sale = new Sale();
        sale.setOrderId(order.getId());
        sale.setProductId(item.getProductId());
        sale.setProductName(item.getProductName());
        sale.setQuantity(item.getQuantity());
        sale.setPrice(item.getPrice());
        sale.setTotalAmount(item.getQuantity() * item.getPrice());
        saleRepository.save(sale);
    }

    // ================= Map-based Methods (internal/analytics) =================

    // Renamed to avoid conflict with DTO method
    public Map<String, Long> getTopSellingProductsMap() {
        return saleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Sale::getProductName,
                        Collectors.summingLong(Sale::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // Renamed to avoid conflict with DTO method
    public Map<String, Double> getMonthlySalesMap() {
        return saleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    public Map<String, Double> getSalesByProduct() {
        return saleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Sale::getProductName,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    // ================= DTO-based Methods (for charts / frontend) =================

    public List<com.example.webapp.dto.ProductSalesDTO> getTopSellingProducts() {
        Map<String, Double> map = getAllSales().stream()
                .collect(Collectors.groupingBy(
                        Sale::getProductName,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));

        return map.entrySet().stream()
                .map(e -> new com.example.webapp.dto.ProductSalesDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<String> getTopSellingProductsNames() {
        return getTopSellingProducts().stream()
                .map(com.example.webapp.dto.ProductSalesDTO::getProductName)
                .collect(Collectors.toList());
    }

    public List<Double> getTopSellingProductsAmounts() {
        return getTopSellingProducts().stream()
                .map(com.example.webapp.dto.ProductSalesDTO::getTotalAmount)
                .collect(Collectors.toList());
    }

    public List<com.example.webapp.dto.MonthlySalesDTO> getMonthlySales() {
        Map<String, Double> monthMap = new TreeMap<>();
        getAllSales().forEach(sale -> {
            String month = sale.getSaleDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthMap.put(month, monthMap.getOrDefault(month, 0.0) + sale.getTotalAmount());
        });

        return monthMap.entrySet().stream()
                .map(e -> new com.example.webapp.dto.MonthlySalesDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<String> getMonthlySalesMonths() {
        return getMonthlySales().stream()
                .map(com.example.webapp.dto.MonthlySalesDTO::getMonthName)
                .collect(Collectors.toList());
    }

    public List<Double> getMonthlySalesAmounts() {
        return getMonthlySales().stream()
                .map(com.example.webapp.dto.MonthlySalesDTO::getTotalAmount)
                .collect(Collectors.toList());
    }
}
