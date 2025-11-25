package com.example.webapp.controller;

import com.example.webapp.model.NewsletterSubscriber;
import com.example.webapp.service.NewsletterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/newsletter")
public class NewsletterDashboardController {

    private final NewsletterService newsletterService;

    public NewsletterDashboardController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    // Newsletter Dashboard - ONLY this controller handles this endpoint
    @GetMapping("/management")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'MARKETING_MANAGER')")
    public String newsletterDashboard(Model model) {
        List<NewsletterSubscriber> subscribers = newsletterService.getAllSubscribers();
        long totalSubscribers = newsletterService.getTotalSubscribers();
        long activeSubscribers = newsletterService.getActiveSubscribersCount();

        model.addAttribute("subscribers", subscribers);
        model.addAttribute("totalSubscribers", totalSubscribers);
        model.addAttribute("activeSubscribers", activeSubscribers);
        model.addAttribute("inactiveSubscribers", totalSubscribers - activeSubscribers);

        return "newsletter-dashboard";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'MARKETING_MANAGER')")
    public String deleteSubscriber(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsletterService.deleteSubscriber(id);
            redirectAttributes.addFlashAttribute("success", "Subscriber deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting subscriber: " + e.getMessage());
        }
        return "redirect:/admin/newsletter/management";
    }

    @PostMapping("/send-newsletter")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'MARKETING_MANAGER')")
    public String sendNewsletter(@RequestParam String subject,
                                 @RequestParam String message,
                                 RedirectAttributes redirectAttributes) {
        try {
            newsletterService.sendNewsletterToAll(subject, message);
            redirectAttributes.addFlashAttribute("success", "Newsletter sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending newsletter: " + e.getMessage());
        }
        return "redirect:/admin/newsletter/management";
    }
}