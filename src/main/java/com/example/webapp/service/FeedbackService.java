package com.example.webapp.service;

import com.example.webapp.model.Feedback;
import com.example.webapp.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    public Feedback saveFeedback(Feedback feedback) {
        return repository.save(feedback);
    }

    public List<Feedback> getAllFeedbacks() {
        return repository.findAll();
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return repository.findById(id);
    }

    public void deleteFeedback(Long id) {
        repository.deleteById(id);
    }
}

