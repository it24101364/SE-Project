package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.repository.OrderRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final JavaMailSender mailSender; // For sending emails

    public OrderService(OrderRepository orderRepository, JavaMailSender mailSender) {
        this.orderRepository = orderRepository;
        this.mailSender = mailSender;
    }

    // ------------------- SAVE OR UPDATE ORDER -------------------
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // ------------------- GET ORDER BY ID -------------------
    public Order getOrderById(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.orElseThrow(() ->
                new RuntimeException("Order not found with ID: " + id)
        );
    }

    // ------------------- GET ALL ORDERS -------------------
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // ------------------- GET ORDERS BY PAYMENT STATUS -------------------
    public List<Order> getOrdersByPaymentStatus(boolean paid) {
        return orderRepository.findByPaid(paid);
    }

    // ------------------- GET ORDERS BY STATUS -------------------
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // ------------------- GET ORDERS BY CUSTOMER EMAIL -------------------
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByUserEmailOrderByOrderDateDesc(email);
    }

    // ------------------- MARK ORDER AS PAID -------------------
    @Transactional
    public void markAsPaid(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setPaid(true);
            orderRepository.save(order);
            try {
                sendEmail(order, "Payment Received",
                        "Your payment has been successfully received for order #" + order.getId());
            } catch (Exception e) {
                System.err.println("Failed to send payment confirmation email: " + e.getMessage());
            }
        });
    }

    // ------------------- UPDATE ORDER STATUS AND TRACKING -------------------
    @Transactional
    public void updateStatus(Long orderId, String status, String trackingNumber) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            order.setTrackingNumber(trackingNumber);
            orderRepository.save(order);

            // Send email notification
            String message = "Your order status has been updated to: " + status + ".";
            if (trackingNumber != null && !trackingNumber.isEmpty()) {
                message += " Tracking Number: " + trackingNumber;
            }
            try {
                sendEmail(order, "Order Update", message);
            } catch (Exception e) {
                System.err.println("Failed to send order update email: " + e.getMessage());
            }
        });
    }

    // ------------------- BULK UPDATE ORDER STATUS -------------------
    @Transactional
    public void updateStatusBulk(List<Long> orderIds, String status, String trackingNumber) {
        orderIds.forEach(id -> updateStatus(id, status, trackingNumber));
    }

    // ------------------- PRIVATE METHOD TO SEND EMAILS -------------------
    private void sendEmail(Order order, String subject, String messageBody) {
        if (order.getUserEmail() != null && !order.getUserEmail().isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUserEmail());
            message.setSubject(subject);
            message.setText("Hello " + order.getFullName() + ",\n\n" +
                    messageBody + "\n\nThank you for shopping with us!");
            mailSender.send(message);
        }
    }
}
