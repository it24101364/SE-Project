package com.example.webapp.controller;

import com.example.webapp.model.Feedback;
import com.example.webapp.service.FeedbackService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute Feedback feedback) {
        service.saveFeedback(feedback);
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')")
    public String feedbackDashboard(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String pending,
            Model model,
            Authentication authentication) {

        System.out.println("=== FEEDBACK DASHBOARD LOADED ===");
        System.out.println("Rating: " + rating + ", Category: " + category + ", Pending: " + pending);

        // Get all feedbacks from service
        List<Feedback> allFeedbacks = service.getAllFeedbacks();
        System.out.println("Total feedbacks from service: " + (allFeedbacks != null ? allFeedbacks.size() : 0));

        if (allFeedbacks == null) {
            allFeedbacks = new ArrayList<>();
        }

        List<Feedback> filteredFeedbacks;

        // Check if any filters are active
        boolean hasActiveFilters = rating != null ||
                (category != null && !category.isEmpty()) ||
                "true".equals(pending);

        if (!hasActiveFilters) {
            // No filters - show all feedbacks
            filteredFeedbacks = new ArrayList<>(allFeedbacks);
            System.out.println("No filters applied, showing all: " + filteredFeedbacks.size() + " feedbacks");
        } else {
            // Apply filters
            filteredFeedbacks = allFeedbacks.stream()
                    .filter(f -> rating == null || (f.getRating() != null && f.getRating().equals(rating)))
                    .filter(f -> category == null || category.isEmpty() ||
                            (f.getCategory() != null && category.equalsIgnoreCase(f.getCategory())))
                    .filter(f -> !"true".equals(pending) ||
                            (f.getAdminReply() == null || f.getAdminReply().isEmpty()))
                    .collect(Collectors.toList());
            System.out.println("Filters applied, showing: " + filteredFeedbacks.size() + " feedbacks");
        }

        // Calculate metrics based on ALL feedbacks
        long fiveStarCount = allFeedbacks.stream()
                .filter(f -> f.getRating() != null && f.getRating() == 5)
                .count();

        long pendingReplies = allFeedbacks.stream()
                .filter(f -> f.getAdminReply() == null || f.getAdminReply().isEmpty())
                .count();

        long otherRatings = allFeedbacks.stream()
                .filter(f -> f.getRating() != null && f.getRating() < 5)
                .count();

        System.out.println("Metrics - 5-star: " + fiveStarCount + ", pending: " + pendingReplies + ", other: " + otherRatings);

        // Add attributes to model
        model.addAttribute("feedbacks", filteredFeedbacks);
        model.addAttribute("fiveStarCount", fiveStarCount);
        model.addAttribute("pendingReplies", pendingReplies);
        model.addAttribute("otherRatings", otherRatings);

        model.addAttribute("rating", rating);
        model.addAttribute("category", category);
        model.addAttribute("pending", pending);

        if (authentication != null) {
            model.addAttribute("adminEmail", authentication.getName());
        }

        return "feedback-dashboard";
    }

    @PostMapping("/reply/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')")
    public String replyFeedback(@PathVariable Long id, @RequestParam String reply) {
        service.getFeedbackById(id).ifPresent(f -> {
            f.setAdminReply(reply);
            service.saveFeedback(f);
        });
        return "redirect:/feedback/dashboard";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'USER_MANAGER')")
    public String deleteFeedback(@PathVariable Long id) {
        service.deleteFeedback(id);
        return "redirect:/feedback/dashboard";
    }

    @GetMapping("/all")
    public String allFeedbacks(Model model) {
        List<Feedback> feedbacks = service.getAllFeedbacks();
        if (feedbacks == null) {
            feedbacks = new ArrayList<>();
        }

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("feedback", new Feedback());
        return "all-feedbacks";
    }
}