package com.example.webapp.repository;

import com.example.webapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserEmail(String userEmail);
    CartItem findByUserEmailAndProductId(String userEmail, Long productId);
}
