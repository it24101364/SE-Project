package com.example.webapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@pcpro.lk}")
    private String fromEmail;

    @Value("${app.email.name:PCPro Store}")
    private String fromName;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send OTP Email
    @Async
    public void sendOtp(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject("Your OTP Code - PCPro.lk 🔐");
            helper.setText(buildOtpEmailContent(otp), true);

            mailSender.send(message);
            System.out.println("✓ OTP email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("✗ Failed to send OTP email to: " + to);
            e.printStackTrace();
        }
    }

    // Send Welcome Email
    @Async
    public void sendWelcomeEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to PCPro.lk - Your Tech Partner! 🎉");
            helper.setText(buildWelcomeEmailContent(toEmail), true);

            mailSender.send(message);
            System.out.println("✓ Welcome email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send welcome email to: " + toEmail);
            e.printStackTrace();
        }
    }

    // Send Password Reset Link
    @Async
    public void sendPasswordResetLink(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - PCPro.lk 🔑");
            helper.setText(buildResetPasswordEmailContent(resetLink), true);

            mailSender.send(message);
            System.out.println("✓ Password reset email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send password reset email to: " + toEmail);
            e.printStackTrace();
        }
    }

    // NEW: Send Newsletter to Single Subscriber
    @Async
    public void sendNewsletter(String toEmail, String subject, String messageContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildNewsletterEmailContent(messageContent, toEmail), true);

            mailSender.send(message);
            System.out.println("✓ Newsletter sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send newsletter to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send newsletter to: " + toEmail, e);
        }
    }

    // NEW: Send Bulk Newsletter (with delay to avoid spam filters)
    @Async
    public void sendBulkNewsletter(String[] toEmails, String subject, String messageContent) {
        int sentCount = 0;
        int failedCount = 0;

        for (String email : toEmails) {
            try {
                sendNewsletter(email, subject, messageContent);
                sentCount++;

                // Add delay between emails to avoid being flagged as spam
                Thread.sleep(1000); // 1 second delay

            } catch (Exception e) {
                failedCount++;
                System.err.println("✗ Failed to send newsletter to: " + email);
            }
        }

        System.out.println("✓ Newsletter sending completed! Sent: " + sentCount + ", Failed: " + failedCount);
    }

    // NEW: Send HTML Newsletter with custom template
    @Async
    public void sendHtmlNewsletter(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✓ HTML Newsletter sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send HTML newsletter to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send HTML newsletter to: " + toEmail, e);
        }
    }

    // ---------------------- EMAIL TEMPLATES ----------------------

    private String buildOtpEmailContent(String otp) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #667eea;'>Your OTP Code - PCPro.lk</h2>"
                + "<div style='background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;'>"
                + "<h1 style='color: #333; margin: 0; font-size: 2.5rem;'>" + otp + "</h1>"
                + "</div>"
                + "<p style='color: #666;'>This OTP will expire in 5 minutes. Please do not share this code with anyone.</p>"
                + "<hr>"
                + "<p style='color: #999; font-size: 12px;'>If you didn't request this OTP, please ignore this email.</p>"
                + "</div>";
    }

    private String buildWelcomeEmailContent(String email) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #667eea;'>Welcome to PCPro.lk! 🎉</h2>"
                + "<p>Hello " + email + ",</p>"
                + "<p>Thank you for joining PCPro.lk - Your trusted partner for all tech needs!</p>"
                + "<div style='background: #f8f9fa; padding: 20px; border-radius: 10px;'>"
                + "<h3 style='color: #333;'>What you can do:</h3>"
                + "<ul>"
                + "<li>Browse latest tech products</li>"
                + "<li>Get exclusive deals and offers</li>"
                + "<li>Fast and secure checkout</li>"
                + "<li>24/7 customer support</li>"
                + "</ul>"
                + "</div>"
                + "<p>Start exploring now: <a href='http://pcpro.lk'>http://pcpro.lk</a></p>"
                + "<hr>"
                + "<p style='color: #999; font-size: 12px;'>PCPro.lk - Your Technology Partner</p>"
                + "</div>";
    }

    private String buildResetPasswordEmailContent(String resetLink) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #667eea;'>Reset Your Password</h2>"
                + "<p>We received a request to reset your password for your PCPro.lk account.</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + resetLink + "' style='background: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;'>Reset Password</a>"
                + "</div>"
                + "<p>This link will expire in 1 hour for security reasons.</p>"
                + "<p style='color: #666;'>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>"
                + "<hr>"
                + "<p style='color: #999; font-size: 12px;'>PCPro.lk Security Team</p>"
                + "</div>";
    }

    // NEW: Newsletter Email Template
    private String buildNewsletterEmailContent(String messageContent, String subscriberEmail) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>"
                + "<h1 style='margin: 0;'>PCPro.lk Newsletter</h1>"
                + "<p style='margin: 10px 0 0; opacity: 0.9;'>Latest Tech Updates & Exclusive Deals</p>"
                + "</div>"
                + "<div style='padding: 30px; background: white; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;'>"
                + "<div style='line-height: 1.6; color: #333;'>"
                + messageContent.replace("\n", "<br>")
                + "</div>"
                + "<hr style='margin: 30px 0; border: none; border-top: 1px solid #e0e0e0;'>"
                + "<div style='text-align: center; color: #666; font-size: 14px;'>"
                + "<p>You received this email because you subscribed to our newsletter.</p>"
                + "<p>"
                + "<a href='http://pcpro.lk/unsubscribe?email=" + subscriberEmail + "' style='color: #667eea; text-decoration: none;'>Unsubscribe</a>"
                + " | "
                + "<a href='http://pcpro.lk' style='color: #667eea; text-decoration: none;'>Visit Our Store</a>"
                + "</p>"
                + "<p>&copy; 2024 PCPro.lk. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    // NEW: Advanced HTML Newsletter Template
    public String buildAdvancedNewsletterTemplate(String subject, String mainContent, String[] featuredProducts) {
        StringBuilder productsHtml = new StringBuilder();

        if (featuredProducts != null && featuredProducts.length > 0) {
            productsHtml.append("<h3 style='color: #333; margin-top: 30px;'>Featured Products</h3>");
            productsHtml.append("<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0;'>");

            for (String product : featuredProducts) {
                productsHtml.append("<div style='border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; text-align: center;'>")
                        .append("<h4 style='margin: 0 0 10px; color: #667eea;'>").append(product).append("</h4>")
                        .append("<p style='color: #666; margin: 0;'>Check out this amazing product!</p>")
                        .append("</div>");
            }
            productsHtml.append("</div>");
        }

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<title>" + subject + "</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; background: #f5f5f5;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: white;'>"
                + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 2.5rem;'>PCPro.lk</h1>"
                + "<p style='margin: 10px 0 0; opacity: 0.9; font-size: 1.2rem;'>" + subject + "</p>"
                + "</div>"
                + "<div style='padding: 40px;'>"
                + "<div style='line-height: 1.6; color: #333; font-size: 16px;'>"
                + mainContent.replace("\n", "<br>")
                + "</div>"
                + productsHtml.toString()
                + "</div>"
                + "<div style='background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px;'>"
                + "<p>PCPro.lk - Your Technology Partner<br>"
                + "Colombo, Sri Lanka</p>"
                + "<p>"
                + "<a href='http://pcpro.lk' style='color: #667eea; margin: 0 10px;'>Website</a> | "
                + "<a href='http://pcpro.lk/unsubscribe' style='color: #667eea; margin: 0 10px;'>Unsubscribe</a> | "
                + "<a href='http://pcpro.lk/contact' style='color: #667eea; margin: 0 10px;'>Contact</a>"
                + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}