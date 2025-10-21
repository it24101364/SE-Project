package com.example.webapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_subscribers")
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Default Constructor
    public NewsletterSubscriber() {
        this.subscribedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Constructor with email
    public NewsletterSubscriber(String email) {
        this.email = email;
        this.subscribedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @PrePersist
    protected void onCreate() {
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "NewsletterSubscriber{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", subscribedAt=" + subscribedAt +
                ", isActive=" + isActive +
                '}';
    }
}