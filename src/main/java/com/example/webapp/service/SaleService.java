package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderItem;
import com.example.webapp.model.Product;
import com.example.webapp.model.Sale;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
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

    // ================= Stock-aware Sale Operations =================

    @Transactional
    public void addSaleFromOrderItem(OrderItem item, Order order) {
        // Create sale record
        Sale sale = new Sale();
        sale.setOrderId(order.getId());
        sale.setProductId(item.getProductId());
        sale.setProductName(item.getProductName());
        sale.setQuantity(item.getQuantity());
        sale.setPrice(item.getPrice());
        sale.setTotalAmount(item.getQuantity() * item.getPrice());
        saleRepository.save(sale);

        // Reduce product stock
        adjustProductStock(item.getProductId(), -item.getQuantity());
    }

    @Transactional
    public void updateSale(Sale updatedSale) {
        Sale existingSale = saleRepository.findById(updatedSale.getId())
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        int oldQty = existingSale.getQuantity();
        int newQty = updatedSale.getQuantity();
        int difference = newQty - oldQty;

        // Update sale details
        existingSale.setQuantity(newQty);
        existingSale.setPrice(updatedSale.getPrice());
        existingSale.setTotalAmount(updatedSale.getTotalAmount());
        existingSale.setSaleDate(updatedSale.getSaleDate());
        saleRepository.save(existingSale);

        // Adjust stock difference
        adjustProductStock(existingSale.getProductId(), -difference);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        // Restore product stock
        adjustProductStock(sale.getProductId(), sale.getQuantity());

        saleRepository.deleteById(id);
    }

    // ================= Helper Method =================

    private void adjustProductStock(Long productId, int quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int newStock = product.getStockCount() + quantityChange;
        product.setStockCount(Math.max(newStock, 0)); // no negative stock
        productRepository.save(product);
    }

    // ================= Analytics & DTO Methods =================

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

    // DTO Methods for frontend charts

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

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id).orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    // For filtered reports (no change to stock)
    public List<String> getTopSellingProductsNames(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(Sale::getProductName, Collectors.summingDouble(Sale::getTotalAmount)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Double> getTopSellingProductsAmounts(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(Sale::getProductName, Collectors.summingDouble(Sale::getTotalAmount)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public List<String> getMonthlySalesMonths(List<Sale> sales) {
        Map<Integer, Double> monthMap = new TreeMap<>();
        for (Sale sale : sales) {
            int month = sale.getSaleDate().getMonthValue();
            monthMap.put(month, monthMap.getOrDefault(month, 0.0) + sale.getTotalAmount());
        }
        return monthMap.keySet().stream()
                .map(m -> java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .collect(Collectors.toList());
    }

    public List<Double> getMonthlySalesAmounts(List<Sale> sales) {
        Map<Integer, Double> monthMap = new TreeMap<>();
        for (Sale sale : sales) {
            int month = sale.getSaleDate().getMonthValue();
            monthMap.put(month, monthMap.getOrDefault(month, 0.0) + sale.getTotalAmount());
        }
        return new ArrayList<>(monthMap.values());
    }

    public double getTotalSales(List<Sale> sales) {
        return sales.stream().mapToDouble(Sale::getTotalAmount).sum();
    }

    public int getTotalUnitsSold(List<Sale> sales) {
        return sales.stream().mapToInt(Sale::getQuantity).sum();
    }

    public double getAverageOrderValue(List<Sale> sales) {
        if (sales.isEmpty()) return 0.0;
        return sales.stream().mapToDouble(Sale::getTotalAmount).average().orElse(0.0);
    }
}
