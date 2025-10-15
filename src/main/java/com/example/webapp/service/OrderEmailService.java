package com.example.webapp.service;

import com.example.webapp.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OrderEmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public OrderEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Updated method — includes payment summary
    public void sendOrderSummary(Order order, String paymentMessage) {
        String subject = "Order Confirmation - Order #" + order.getId();

        String body = "Dear " + order.getFullName() + ",\n\n" +
                "Thank you for your purchase!\n\n" +
                "Order Details:\n" +
                "Order ID: " + order.getId() + "\n" +
                "Total Amount: Rs. " + order.getTotalAmount() + "\n" +
                "Payment Type: " + order.getPaymentType() + "\n" +
                "Payment Status: " + (order.isPaid() ? "Paid" : "Pending") + "\n\n" +
                "Payment Info:\n" + paymentMessage + "\n\n" +
                "Shipping To:\n" +
                order.getFullName() + "\n" +
                order.getAddress() + ", " + order.getCity() + "\n" +
                order.getCountry() + " - " + order.getPostalCode() + "\n" +
                "Phone: " + order.getPhone() + "\n\n" +
                "We'll notify you once your order has been shipped.\n\n" +
                "Thank you for shopping with us!\n\n" +
                "-- Spare Parts Store";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUserEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
