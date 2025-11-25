package com.example.webapp.repository;

import com.example.webapp.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Custom query methods for filtering
    List<Feedback> findByRating(Integer rating);

    List<Feedback> findByCategory(String category);

    List<Feedback> findByAdminReplyIsNull();

    long countByRating(Integer rating);

    // Custom query for complex filtering
    @Query("SELECT f FROM Feedback f WHERE " +
            "(:rating IS NULL OR f.rating = :rating) AND " +
            "(:category IS NULL OR f.category = :category) AND " +
            "(:pending IS NULL OR (:pending = true AND f.adminReply IS NULL))")
    List<Feedback> findWithFilters(Integer rating, String category, Boolean pending);
}