package com.example.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("/complete")
    public String profileComplete(Model model) {
        model.addAttribute("page", "profile-complete");
        return "profile-complete";
    }

    @PostMapping("/complete")
    public String saveProfileComplete() {
        // Placeholder - frontend only for now
        // In future, this will save profile data to database
        return "redirect:/profile/complete?success=true";
    }
}




