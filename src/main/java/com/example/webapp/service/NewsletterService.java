package com.example.webapp.service;

import com.example.webapp.model.NewsletterSubscriber;
import com.example.webapp.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final MailService mailService;  // Changed from EmailService

    public NewsletterService(NewsletterSubscriberRepository subscriberRepository,
                             MailService mailService) {  // Changed from EmailService
        this.subscriberRepository = subscriberRepository;
        this.mailService = mailService;
    }

    @Transactional
    public boolean subscribe(String email) {
        try {
            String normalizedEmail = email.trim().toLowerCase();

            if (subscriberRepository.existsByEmail(normalizedEmail)) {
                System.out.println("✗ Email already exists: " + normalizedEmail);
                return false;
            }

            NewsletterSubscriber subscriber = new NewsletterSubscriber(normalizedEmail);
            NewsletterSubscriber saved = subscriberRepository.save(subscriber);

            System.out.println("✓ Subscriber saved successfully: " + saved);

            // Send welcome email asynchronously
            try {
                mailService.sendWelcomeEmail(normalizedEmail);
            } catch (Exception e) {
                System.err.println("⚠ Email sending failed, but subscription was successful");
                // Don't throw - subscription should succeed even if email fails
            }

            return true;

        } catch (Exception e) {
            System.err.println("✗ Error saving subscriber: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save subscriber", e);
        }
    }
}