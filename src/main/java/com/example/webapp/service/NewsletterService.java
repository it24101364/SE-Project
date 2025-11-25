package com.example.webapp.service;

import com.example.webapp.model.NewsletterSubscriber;
import com.example.webapp.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final MailService mailService;

    public NewsletterService(NewsletterSubscriberRepository subscriberRepository,
                             MailService mailService) {
        this.subscriberRepository = subscriberRepository;
        this.mailService = mailService;
    }

    // FIXED: Add the missing getActiveSubscribers method
    public List<NewsletterSubscriber> getActiveSubscribers() {
        return subscriberRepository.findByIsActiveTrue();
    }

    // Other necessary methods
    public boolean subscribe(String email) {
        if (subscriberRepository.existsByEmail(email)) {
            return false;
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber(email);
        subscriberRepository.save(subscriber);
        return true;
    }

    public List<NewsletterSubscriber> getAllSubscribers() {
        return subscriberRepository.findAll();
    }

    public void deleteSubscriber(Long id) {
        if (subscriberRepository.existsById(id)) {
            subscriberRepository.deleteById(id);
        }
    }

    public void unsubscribe(String email) {
        subscriberRepository.findByEmail(email).ifPresent(subscriber -> {
            subscriber.setActive(false);
            subscriberRepository.save(subscriber);
        });
    }

    public long getTotalSubscribers() {
        return subscriberRepository.count();
    }

    public long getActiveSubscribersCount() {
        return subscriberRepository.countActiveSubscribers();
    }

    // FIXED: Updated sendNewsletterToAll method
    public void sendNewsletterToAll(String subject, String message) {
        List<NewsletterSubscriber> activeSubscribers = getActiveSubscribers(); // This should work now

        if (activeSubscribers.isEmpty()) {
            System.out.println("No active subscribers to send newsletter to.");
            return;
        }

        System.out.println("Sending newsletter to " + activeSubscribers.size() + " subscribers...");

        // Extract emails
        String[] subscriberEmails = activeSubscribers.stream()
                .map(NewsletterSubscriber::getEmail)
                .toArray(String[]::new);

        try {
            // Use the MailService to send bulk newsletter
            mailService.sendBulkNewsletter(subscriberEmails, subject, message);
            System.out.println("✓ Newsletter sending initiated for " + activeSubscribers.size() + " subscribers!");

        } catch (Exception e) {
            System.err.println("✗ Failed to send newsletter: " + e.getMessage());
            throw new RuntimeException("Failed to send newsletter", e);
        }
    }

    // Optional: Method to send HTML newsletter
    public void sendHtmlNewsletterToAll(String subject, String message) {
        List<NewsletterSubscriber> activeSubscribers = getActiveSubscribers(); // This should work now

        if (activeSubscribers.isEmpty()) {
            System.out.println("No active subscribers to send newsletter to.");
            return;
        }

        System.out.println("Sending HTML newsletter to " + activeSubscribers.size() + " subscribers...");

        for (NewsletterSubscriber subscriber : activeSubscribers) {
            try {
                String htmlContent = mailService.buildAdvancedNewsletterTemplate(
                        subject, message, new String[]{"Gaming Laptops", "Mechanical Keyboards", "Wireless Mice"}
                );

                mailService.sendHtmlNewsletter(subscriber.getEmail(), subject, htmlContent);

                // Small delay to avoid being flagged as spam
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("✗ Failed to send HTML newsletter to: " + subscriber.getEmail());
                // Continue with other subscribers even if one fails
            }
        }

        System.out.println("✓ HTML Newsletter sending completed!");
    }
}