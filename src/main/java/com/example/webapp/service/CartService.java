package com.example.webapp.service;

import com.example.webapp.model.CartItem;
import com.example.webapp.model.Product;
import com.example.webapp.repository.CartRepository;
import com.example.webapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCartItems(String userEmail) {
        return cartRepository.findByUserEmail(userEmail);
    }

    public void addToCart(String userEmail, Long productId) {
        CartItem item = cartRepository.findByUserEmailAndProductId(userEmail, productId);
        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
        } else {
            item = new CartItem();
            item.setUserEmail(userEmail);
            Product product = productRepository.findById(productId).orElseThrow();
            item.setProduct(product);
            item.setQuantity(1);
        }
        cartRepository.save(item);
    }

    public void removeItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public void clearCart(String userEmail) {
        List<CartItem> items = cartRepository.findByUserEmail(userEmail);
        cartRepository.deleteAll(items);
    }

    public void buyNow(String userEmail, Long productId) {
        List<CartItem> existingItems = cartRepository.findByUserEmail(userEmail);
        if (!existingItems.isEmpty()) {
            cartRepository.deleteAll(existingItems);
        }

        Product product = productRepository.findById(productId).orElseThrow();
        CartItem item = new CartItem();
        item.setUserEmail(userEmail);
        item.setProduct(product);
        item.setQuantity(1);

        cartRepository.save(item);
    }

    // ✅ Final, correct version
    public void updateQuantity(Long cartId, int quantity) {
        CartItem item = cartRepository.findById(cartId).orElseThrow();
        Product product = item.getProduct();

        // Check against stock
        if (quantity > product.getStockCount()) {
            throw new IllegalArgumentException(
                    "Quantity exceeds available stock (" + product.getStockCount() + ")"
            );
        }

        // Prevent zero or negative quantity
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        item.setQuantity(quantity);
        cartRepository.save(item);
    }
}
