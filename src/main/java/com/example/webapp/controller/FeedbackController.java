package com.example.webapp.controller;

import com.example.webapp.model.Feedback;
import com.example.webapp.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute Feedback feedback) {
        service.saveFeedback(feedback);  // ✅ use 'service' instead of 'feedbackService'
        return "redirect:/feedback/all";
    }



    @GetMapping("/dashboard")
    public String feedbackDashboard(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String pending,
            Model model) {

        List<Feedback> feedbacks = service.getAllFeedbacks();

        // Apply filters
        if (rating != null) {
            feedbacks = feedbacks.stream()
                    .filter(f -> f.getRating() == rating)
                    .toList();
        }

        if (category != null && !category.isEmpty()) {
            feedbacks = feedbacks.stream()
                    .filter(f -> f.getCategory().equalsIgnoreCase(category))
                    .toList();
        }

        if ("true".equals(pending)) {
            feedbacks = feedbacks.stream()
                    .filter(f -> f.getAdminReply() == null || f.getAdminReply().isEmpty())
                    .toList();
        }

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("fiveStarCount", feedbacks.stream().filter(f -> f.getRating() == 5).count());
        model.addAttribute("pendingReplies", feedbacks.stream().filter(f -> f.getAdminReply() == null || f.getAdminReply().isEmpty()).count());
        model.addAttribute("otherRatings", feedbacks.stream().filter(f -> f.getRating() < 5).count());

        // Keep the selected filters in the template
        model.addAttribute("rating", rating);
        model.addAttribute("category", category);
        model.addAttribute("pending", pending);

        return "feedback-dashboard";
    }




    // Admin Reply
    @PostMapping("/reply/{id}")
    public String replyFeedback(@PathVariable Long id, @RequestParam String reply) {
        service.getFeedbackById(id).ifPresent(f -> {
            f.setAdminReply(reply);
            service.saveFeedback(f);
        });
        return "redirect:/feedback/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        service.deleteFeedback(id);
        return "redirect:/feedback/dashboard";
    }

    @GetMapping("/all")
    public String allFeedbacks(Model model) {
        List<Feedback> feedbacks = service.getAllFeedbacks();
        model.addAttribute("feedbacks", feedbacks);
        return "all-feedbacks";
    }



}
