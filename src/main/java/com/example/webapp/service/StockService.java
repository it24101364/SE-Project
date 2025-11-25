package com.example.webapp.service;

import com.example.webapp.model.Product;
import com.example.webapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Using the new repository method
    public List<Product> getLowStockProducts() {
        return productRepository.findByStockCountLessThan(10);
    }

    // Alternative method with custom threshold
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockCountLessThan(threshold);
    }

    // Using the null-safe version
    public List<Product> getLowStockProductsNotNull() {
        return productRepository.findLowStockProductsNotNull(10);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void addProduct(String name, String category, Integer stockCount) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setStockCount(stockCount != null ? stockCount : 0);
        productRepository.save(product);
    }

    public void deleteProduct(Long productId) {
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
        }
    }

    public void restockProduct(Long productId, Integer addStock) {
        if (addStock == null || addStock <= 0) {
            throw new IllegalArgumentException("Add stock value must be positive");
        }

        productRepository.findById(productId).ifPresent(product -> {
            Integer currentStock = product.getStockCount() != null ? product.getStockCount() : 0;
            product.setStockCount(currentStock + addStock);
            productRepository.save(product);
        });
    }

    // Additional useful methods
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStockCountEquals(0);
    }

    public List<Product> getWellStockedProducts() {
        return productRepository.findByStockCountGreaterThan(10);
    }
}