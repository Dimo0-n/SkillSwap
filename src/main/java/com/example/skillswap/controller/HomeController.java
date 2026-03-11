package com.example.skillswap.controller;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.service.AnnounceService;
import com.example.skillswap.service.ProfileCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

    // TODO de adaugat o validare de skill-uri

    @Autowired
    private AnnounceService announceService;

    @Autowired
    private ProfileCompletionService profileCompletionService;

    @GetMapping("/index")
    public String home(Model model) {
        List<Announce> latest5Announces = announceService.getLatest5Announces();
        model.addAttribute("latest5Announces", latest5Announces);
        model.addAttribute("page", "index");
        return "index";
    }

    @GetMapping("/chat")
    public String chat(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return profileCompletionService.getRequiredRedirectView();
        }

        model.addAttribute("page", "chat");
        return "chat";
    }

    @GetMapping("/chat-history")
    public String chatHistory(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return profileCompletionService.getRequiredRedirectView();
        }

        model.addAttribute("page", "chat-history");
        // Pass current user info to frontend for WebSocket
        if (principal != null) {
            model.addAttribute("currentUserEmail", principal.getName());
        }
        return "chat-history";
    }

    @GetMapping("/post-details")
    public String detailsPostGallery() {
        return "announce-details";
    }

    @GetMapping("/typography")
    public String typography() {
        return "typography";
    }

    @GetMapping("/meeting")
    public String meeting() {
        return "jitsi-meet";
    }

}
