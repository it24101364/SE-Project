package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OrderEmailService {

    private final JavaMailSender mailSender;

    public OrderEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderSummary(Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(order.getUserEmail());
            helper.setSubject("Order Confirmation - Order #" + order.getId());

            StringBuilder body = new StringBuilder();
            body.append("<h2>Thank you for your order!</h2>");
            body.append("<p>Your order has been placed successfully. Here are the details:</p>");
            body.append("<p><b>Order ID:</b> ").append(order.getId()).append("</p>");
            body.append("<p><b>Total Amount:</b> Rs. ").append(order.getTotalAmount()).append("</p>");
            body.append("<p><b>Payment Method:</b> ").append(order.getPaymentType()).append("</p>");
            body.append("<h3>Items:</h3>");
            body.append("<table border='1' cellpadding='8' cellspacing='0'>");
            body.append("<tr><th>Product</th><th>Quantity</th><th>Price</th></tr>");
            for (OrderItem item : order.getItems()) {
                body.append("<tr>")
                        .append("<td>").append(item.getProductName()).append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>Rs. ").append(item.getPrice()).append("</td>")
                        .append("</tr>");
            }
            body.append("</table>");
            body.append("<p>We’ll notify you once your order is shipped.</p>");
            body.append("<br><p>Regards,<br/>Spare Parts Management System</p>");

            helper.setText(body.toString(), true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
