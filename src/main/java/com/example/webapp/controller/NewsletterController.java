package com.example.webapp.controller;

import com.example.webapp.service.NewsletterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email, RedirectAttributes redirectAttributes) {

        System.out.println("📧 Received subscription request for: " + email);

        try {
            // Basic validation
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                redirectAttributes.addFlashAttribute("error", "Invalid email address!");
                return "redirect:/";
            }

            boolean subscribed = newsletterService.subscribe(email.trim().toLowerCase());

            if (subscribed) {
                redirectAttributes.addFlashAttribute("success", "Successfully subscribed! Thank you.");
                System.out.println("✓ Subscription successful for: " + email);
            } else {
                redirectAttributes.addFlashAttribute("info", "This email is already subscribed!");
                System.out.println("ℹ Email already subscribed: " + email);
            }

        } catch (Exception e) {
            System.err.println("✗ Error in subscription: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Subscription failed. Please try again.");
        }

        return "redirect:/";
    }
}