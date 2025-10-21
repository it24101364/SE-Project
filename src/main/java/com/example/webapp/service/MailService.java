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

    // Updated OTP method with HTML design
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

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send OTP email to: " + to);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error sending OTP email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New method for newsletter welcome email
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

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send welcome email to: " + toEmail);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // HTML email template for OTP
    private String buildOtpEmailContent(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        color: white;
                        padding: 40px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        font-size: 16px;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .content h2 {
                        color: #667eea;
                        margin-top: 0;
                        font-size: 24px;
                    }
                    .otp-box {
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        color: white;
                        padding: 25px;
                        border-radius: 15px;
                        margin: 30px 0;
                        box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
                    }
                    .otp-code {
                        font-size: 48px;
                        font-weight: bold;
                        letter-spacing: 10px;
                        margin: 10px 0;
                        font-family: 'Courier New', monospace;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                    }
                    .otp-label {
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                        opacity: 0.9;
                    }
                    .warning-box {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                        text-align: left;
                    }
                    .warning-box strong {
                        color: #856404;
                    }
                    .info-box {
                        background: #d1ecf1;
                        border-left: 4px solid #0dcaf0;
                        padding: 15px 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                        text-align: left;
                    }
                    .info-box strong {
                        color: #0c5460;
                    }
                    .timer {
                        background: #f8f9fa;
                        padding: 15px;
                        border-radius: 10px;
                        margin: 20px 0;
                        display: inline-block;
                    }
                    .timer-icon {
                        font-size: 24px;
                        color: #ff4757;
                    }
                    .timer-text {
                        color: #ff4757;
                        font-weight: bold;
                        font-size: 18px;
                        margin: 5px 0;
                    }
                    .security-tips {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 10px;
                        margin: 25px 0;
                        text-align: left;
                    }
                    .security-tips h3 {
                        color: #667eea;
                        margin-top: 0;
                        font-size: 18px;
                    }
                    .security-tips ul {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    .security-tips li {
                        margin: 8px 0;
                        color: #555;
                    }
                    .footer {
                        background: #1a1a2e;
                        color: rgba(255,255,255,0.7);
                        padding: 30px;
                        text-align: center;
                        font-size: 14px;
                    }
                    .footer a {
                        color: #00d9ff;
                        text-decoration: none;
                    }
                    .icon {
                        font-size: 48px;
                        margin-bottom: 15px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Verification Code</h1>
                        <p>PCPro.lk Security</p>
                    </div>
                    
                    <div class="content">
                        <div class="icon">🛡️</div>
                        <h2>Your One-Time Password</h2>
                        <p>Use this code to complete your verification. Do not share this code with anyone.</p>
                        
                        <div class="otp-box">
                            <div class="otp-label">Your OTP Code</div>
                            <div class="otp-code">""" + otp + """
                            </div>
                        </div>
                        
                        <div class="timer">
                            <div class="timer-icon">⏱️</div>
                            <div class="timer-text">Expires in 5 minutes</div>
                        </div>
                        
                        <div class="warning-box">
                            <strong>⚠️ Important:</strong> This code will expire in 5 minutes. If you didn't request this code, please ignore this email or contact our support team immediately.
                        </div>
                        
                        <div class="info-box">
                            <strong>ℹ️ Need Help?</strong> If you're having trouble, please contact our support team at info@pcpro.lk or call +94 77 123 4567
                        </div>
                        
                        <div class="security-tips">
                            <h3>🔒 Security Tips</h3>
                            <ul>
                                <li>Never share your OTP code with anyone</li>
                                <li>PCPro.lk will never ask for your OTP via phone or email</li>
                                <li>Use this code only on our official website</li>
                                <li>Be aware of phishing attempts</li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Contact Us</strong></p>
                        <p>📧 Email: info@pcpro.lk<br>
                        📞 Phone: +94 77 123 4567<br>
                        📍 Address: 123 Tech Street, Colombo, Sri Lanka</p>
                        
                        <p style="margin-top: 20px; font-size: 12px;">
                            This is an automated security email from PCPro.lk<br>
                            Please do not reply to this email.
                        </p>
                        
                        <p style="margin-top: 10px; font-size: 11px; color: rgba(255,255,255,0.5);">
                            © 2025 PCPro.lk. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    // HTML email template for welcome email
    private String buildWelcomeEmailContent(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #00d9ff, #667eea);
                        color: white;
                        padding: 40px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .content h2 {
                        color: #00d9ff;
                        margin-top: 0;
                    }
                    .features {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                    .feature-item {
                        padding: 10px 0;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    .feature-item:last-child {
                        border-bottom: none;
                    }
                    .feature-item strong {
                        color: #667eea;
                    }
                    .cta-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #00d9ff, #667eea);
                        color: white;
                        padding: 15px 40px;
                        text-decoration: none;
                        border-radius: 25px;
                        margin: 20px 0;
                        font-weight: bold;
                        box-shadow: 0 4px 15px rgba(0, 217, 255, 0.3);
                    }
                    .footer {
                        background: #1a1a2e;
                        color: rgba(255,255,255,0.7);
                        padding: 30px;
                        text-align: center;
                        font-size: 14px;
                    }
                    .footer a {
                        color: #00d9ff;
                        text-decoration: none;
                    }
                    .social-links {
                        margin: 20px 0;
                    }
                    .social-links a {
                        display: inline-block;
                        margin: 0 10px;
                        color: #00d9ff;
                        text-decoration: none;
                    }
                    .promo-code {
                        background: #fffacd;
                        padding: 5px 10px;
                        border-radius: 5px;
                        font-weight: bold;
                        color: #667eea;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to PCPro.lk!</h1>
                        <p>Your Premium Computer Parts Destination</p>
                    </div>
                    
                    <div class="content">
                        <h2>Hello Tech Enthusiast! 👋</h2>
                        <p>Thank you for subscribing to our newsletter. We're thrilled to have you as part of the PCPro.lk family!</p>
                        
                        <p>As a valued subscriber, you'll receive:</p>
                        
                        <div class="features">
                            <div class="feature-item">
                                <strong>🎁 Exclusive Deals:</strong> Early access to special discounts and promotions
                            </div>
                            <div class="feature-item">
                                <strong>🚀 New Arrivals:</strong> Be the first to know about latest products
                            </div>
                            <div class="feature-item">
                                <strong>💡 Tech Tips:</strong> Expert advice on building and upgrading your PC
                            </div>
                            <div class="feature-item">
                                <strong>📰 Industry News:</strong> Latest updates from the tech world
                            </div>
                            <div class="feature-item">
                                <strong>🎯 Personalized Recommendations:</strong> Products tailored to your interests
                            </div>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="http://localhost:8080/products" class="cta-button">Start Shopping Now</a>
                        </p>
                        
                        <p><strong>Special Welcome Offer:</strong> Use code <span class="promo-code">WELCOME10</span> for 10% off your first purchase!</p>
                        
                        <p>If you have any questions, our support team is here to help 24/7.</p>
                        
                        <p>Happy Shopping!<br>
                        <strong>The PCPro.lk Team</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Contact Us</strong></p>
                        <p>📧 Email: info@pcpro.lk<br>
                        📞 Phone: +94 77 123 4567<br>
                        📍 Address: 123 Tech Street, Colombo, Sri Lanka</p>
                        
                        <div class="social-links">
                            <a href="#">Facebook</a> |
                            <a href="#">Twitter</a> |
                            <a href="#">Instagram</a> |
                            <a href="#">LinkedIn</a>
                        </div>
                        
                        <p style="margin-top: 20px; font-size: 12px;">
                            You're receiving this email because you subscribed to PCPro.lk newsletter.<br>
                            <a href="#">Unsubscribe</a> | <a href="#">Update Preferences</a>
                        </p>
                        
                        <p style="margin-top: 10px; font-size: 11px; color: rgba(255,255,255,0.5);">
                            © 2025 PCPro.lk. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}