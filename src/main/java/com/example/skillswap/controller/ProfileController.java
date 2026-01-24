package com.example.skillswap.controller;

import com.example.skillswap.entity.Profil;
import com.example.skillswap.enums.Availability;
import com.example.skillswap.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfilService profileService;

    @GetMapping("/complete")
    public String profileComplete(Model model) {
        model.addAttribute("profil", new Profil());
        model.addAttribute("page", "profile-complete");
        return "profile-complete";
    }

    @PostMapping("/save")
    public String saveProfileComplete(
            @ModelAttribute("profil") Profil profil,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication auth
    ) throws IOException {

        String email = auth.getName();

        profileService.saveProfile(profil, profilePicture, email);
        return "redirect:/profile/complete?success=true";
    }


}






