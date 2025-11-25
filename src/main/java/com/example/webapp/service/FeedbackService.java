package com.example.webapp.service;

import com.example.webapp.model.Feedback;
import com.example.webapp.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public List<Feedback> getAllFeedbacks() {
        try {
            List<Feedback> feedbacks = feedbackRepository.findAll();
            // Sort by ID descending to show newest first
            if (feedbacks != null) {
                feedbacks.sort((f1, f2) -> f2.getId().compareTo(f1.getId()));
            }
            return feedbacks;
        } catch (Exception e) {
            // Log the error properly in production
            System.err.println("Error fetching feedbacks: " + e.getMessage());
            return List.of(); // Return empty list instead of null
        }
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    // Additional useful methods
    public List<Feedback> getFeedbacksByRating(Integer rating) {
        return feedbackRepository.findByRating(rating);
    }

    public List<Feedback> getFeedbacksByCategory(String category) {
        return feedbackRepository.findByCategory(category);
    }

    public List<Feedback> getFeedbacksPendingReply() {
        return feedbackRepository.findByAdminReplyIsNull();
    }

    public long getTotalFeedbackCount() {
        return feedbackRepository.count();
    }

    public long getFeedbackCountByRating(Integer rating) {
        return feedbackRepository.countByRating(rating);
    }
}