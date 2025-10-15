package com.example.webapp.repository;

import com.example.webapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByPaid(boolean paid);
    List<Order> findByStatus(String status);
    // Find orders for a specific user, ordered by date
    List<Order> findByUserEmailOrderByOrderDateDesc(String userEmail);

}
