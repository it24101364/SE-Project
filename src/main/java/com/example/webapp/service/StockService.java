package com.example.webapp.service;

import com.example.webapp.model.Product;
import com.example.webapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getStockCount() < 10) // low stock threshold
                .collect(Collectors.toList());
    }

    public void addProduct(String name, int stockCount) {
        Product product = new Product();
        product.setName(name);
        product.setStockCount(stockCount);
        productRepository.save(product);
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public void restockProduct(Long productId, int addStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setStockCount(product.getStockCount() + addStock);
        productRepository.save(product);
    }

    public List<Product> filterProducts(String filter) {
        if ("low".equals(filter)) {
            return productRepository.findAll()
                    .stream()
                    .filter(p -> p.getStockCount() < 10)
                    .toList();
        } else if ("high".equals(filter)) {
            return productRepository.findAll()
                    .stream()
                    .filter(p -> p.getStockCount() >= 10)
                    .toList();
        } else {
            return getAllProducts();
        }
    }

}
