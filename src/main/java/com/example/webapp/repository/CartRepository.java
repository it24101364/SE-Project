package com.example.webapp.repository;

import com.example.webapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserEmail(String userEmail);

    CartItem findByUserEmailAndProductId(String userEmail, Long productId);

    // 🔥 Add this line — allows deleting all cart items for a product
    void deleteByProductId(Long productId);

    // (Optional) Check if any cart items exist for a product
    boolean existsByProductId(Long productId);
}
