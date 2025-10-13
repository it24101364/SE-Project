package com.example.webapp.service;

import com.example.webapp.model.Product;
import com.example.webapp.repository.CartRepository;
import com.example.webapp.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository; // ✅ add this

    public ProductService(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Save product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    // Update product
    public Product updateProduct(Long id, Product updatedProduct) {
        Product product = getProductById(id);
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setImageUrl(updatedProduct.getImageUrl());
        return productRepository.save(product);
    }

    // ✅ Delete product safely (first delete from cart_items)
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        // Delete any cart items referencing this product
        cartRepository.deleteByProductId(id);

        // Now delete the product itself
        productRepository.deleteById(id);
    }

    // Reduce stock
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        int newStock = Math.max(product.getStockCount() - quantity, 0);
        product.setStockCount(newStock);
        productRepository.save(product);
    }
}
