package com.example.skillswap.controller;

import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.enums.Availability;
import com.example.skillswap.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

        if (auth == null) {
            return "redirect:/login";
        }

        String email = auth.getName();

        profileService.saveProfile(profil, profilePicture, email);
        return "redirect:/profile/complete?success=true";
    }

    @GetMapping("")
    public String profilePage(Model model, Authentication auth) {

        ProfilDto profile = profileService.getProfileForView(auth.getName());

        model.addAttribute("profile", profile);
        return "profil";
    }

    @GetMapping("/profile/image/{email}")
    @ResponseBody
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String email) throws IOException {

        byte[] image = profileService.getProfileImageByEmail(email);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(image);
    }



}






