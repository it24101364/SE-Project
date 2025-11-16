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

    // NEW: Send Password Reset Link
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

    // ---------------------- EMAIL TEMPLATES ----------------------

    private String buildOtpEmailContent(String otp) {
        return "<h2>Your OTP Code: " + otp + "</h2>"
                + "<p>Expires in 5 minutes. Do not share this code.</p>";
    }

    private String buildWelcomeEmailContent(String email) {
        return "<h2>Welcome " + email + " to PCPro.lk!</h2>"
                + "<p>Thank you for registering. Enjoy exclusive deals and updates!</p>";
    }

    private String buildResetPasswordEmailContent(String resetLink) {
        return "<h2>Password Reset Request</h2>"
                + "<p>Click the link below to reset your password:</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>"
                + "<p>If you did not request this, please ignore this email.</p>";
    }
}
