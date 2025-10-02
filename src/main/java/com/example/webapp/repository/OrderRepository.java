package com.example.webapp.repository;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderStatus;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
}