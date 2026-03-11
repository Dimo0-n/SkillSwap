package com.example.skillswap.controller;

import com.example.skillswap.dto.AnnounceDto;
import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.service.AnnounceService;
import com.example.skillswap.service.ProfileService;
import com.example.skillswap.service.AnnounceImageService;
import com.example.skillswap.service.ProfileCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
class AnnounceController {

    @Autowired
    private AnnounceService announceService;

    @Autowired
    private AnnounceImageService announceImageService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileCompletionService profileCompletionService;

    @GetMapping("/announces-list")
    public String categoryGrid(Model model) {
        List<Announce> announcesList = announceService.getAnnouncesList();
        model.addAttribute("announcesList", announcesList);
        model.addAttribute("page", "announces-list");
        return "announces-list";
    }

    @GetMapping("/announces/new")
    public String addAnnounce(Model model, Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        if (!profileCompletionService.isProfileCompleted(auth)) {
            return profileCompletionService.getRequiredRedirectView();
        }

        AnnounceDto announce = new AnnounceDto();
        announce.setAuthor(resolveDefaultAuthor(auth));
        model.addAttribute("announce", announce);
        return "announce-create";
    }

    @PostMapping("/announce/save")
    public String saveAnnounce(@Valid @ModelAttribute("announce") AnnounceDto announceDto, BindingResult bindingResult,
            Authentication auth, Model model) {

        if (auth == null) {
            return "redirect:/login";
        }

        if (!profileCompletionService.isProfileCompleted(auth)) {
            return profileCompletionService.getRequiredRedirectView();
        }

        if (bindingResult.hasErrors()) {
            return "announce-create";
        }

        String safePath = announceImageService.safePath(
                announceDto.getCategoryOffered(),
                announceDto.getImageKey());
        announceDto.setImagePath(safePath);

        announceService.save(announceDto, auth);

        return "redirect:/index";
    }

    private String resolveDefaultAuthor(Authentication auth) {
        try {
            return profileService.getProfileForView(auth.getName()).getName();
        } catch (RuntimeException exception) {
            return auth.getName();
        }
    }

    @GetMapping("/announce-details/{id}")
    public String postDetails(@PathVariable Long id, Model model, Authentication auth) {

        AnnounceDto announceDto = announceService.getAnnounceById(id);

        Long userId = announceDto.getUserId();
        ProfilDto profilDto = profileService.getAuthorByUserId(userId);

        model.addAttribute("announce", announceDto);
        model.addAttribute("profile", profilDto);
        model.addAttribute("currentUserProfileCompleted", profileCompletionService.isProfileCompleted(auth));
        model.addAttribute("profileCompletionRequiredUrl", ProfileCompletionService.REQUIRED_REDIRECT_PATH);

        return "announce-details";
    }

}
