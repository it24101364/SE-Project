package com.example.webapp.repository;

import com.example.webapp.model.CartItem;
import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    void deleteByUser(User user);
}