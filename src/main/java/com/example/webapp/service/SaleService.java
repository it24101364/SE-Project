package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderItem;
import com.example.webapp.model.Sale;
import com.example.webapp.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

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

    public Map<String, Double> getSalesByProduct() {
        return saleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Sale::getProductName,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    public Map<String, Long> getTopSellingProducts() {
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
                        java.util.LinkedHashMap::new
                ));
    }

    public Map<String, Double> getMonthlySales() {
        return saleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getMonth().toString() + " " + sale.getSaleDate().getYear(),
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
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
}