package com.example.webapp.repository;

import com.example.webapp.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    boolean existsByEmail(String email);
    Optional<NewsletterSubscriber> findByEmail(String email);

    // ADD THIS METHOD if it's missing
    List<NewsletterSubscriber> findByIsActiveTrue();

    @Query("SELECT COUNT(s) FROM NewsletterSubscriber s WHERE s.isActive = true")
    long countActiveSubscribers();
}