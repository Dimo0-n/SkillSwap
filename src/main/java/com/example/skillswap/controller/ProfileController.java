package com.example.skillswap.controller;

import com.example.skillswap.dto.CreateProfileCommentDto;
import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.security.CustomUserDetails;
import com.example.skillswap.service.AnnounceService;
import com.example.skillswap.service.ProfileCommentService;
import com.example.skillswap.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AnnounceService announceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileCommentService profileCommentService;

    @GetMapping("")
    public String profilePage(Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        ProfilDto profile;
        try {
            profile = profileService.getProfileForView(auth.getName());
        } catch (RuntimeException ex) {
            return "redirect:/profile/complete";
        }

        populateProfilePageModel(model, profile, extractCurrentUserId(auth));
        return "profil";
    }

    @GetMapping("/{userId}")
    public String publicProfilePage(@PathVariable Long userId, Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        ProfilDto profile = profileService.getProfileForUserId(userId);
        populateProfilePageModel(model, profile, extractCurrentUserId(auth));
        return "profil";
    }

    @GetMapping("/complete")
    public String profileComplete(Model model, Authentication auth) {
        ProfilDto dto;
        if (auth != null) {
            try {
                dto = profileService.getProfileForView(auth.getName());
            } catch (RuntimeException ex) {
                dto = new ProfilDto();
            }
        } else {
            dto = new ProfilDto();
        }
        model.addAttribute("profile", dto);
        model.addAttribute("page", "profile-complete");
        return "profile-complete";
    }

    @PostMapping("/save")
    public String saveProfileComplete(
            @ModelAttribute("profile") ProfilDto profilDto,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication auth
    ) throws IOException {

        if (auth == null) {
            return "redirect:/login";
        }

        String email = auth.getName();

        profileService.saveProfile(profilDto, profilePicture, email);
        return "redirect:/profile/complete?success=true";
    }

    //Afisarea anunturilor care le-a postat user-ul
    //logica se duce la AnnounceService
    @GetMapping("/user-announces-list")
    public String userAnnouncesList(Authentication auth, Model model) {

        if (auth == null) {
            return "redirect:/login";
        }

        Long userId = extractCurrentUserId(auth);

        List<Announce> announcesList = announceService.getAnnouncesListByEmail(userId);
        model.addAttribute("announcesList", announcesList);

        return "user-announces-list";
    }

    //Stergerea unui anunt de catre user dupa id-ul la anunt
    @PreAuthorize("@announceSecurity.isOwner(#id, authentication)")
    @PostMapping("/user-announces-list/delete/{id}")
    public String deleteAnnounceById(@PathVariable Long id) {
        announceService.deleteAnnounceById(id);
        return "redirect:/profile/user-announces-list";
    }

    private void populateProfilePageModel(Model model, ProfilDto profile, Long currentUserId) {
        Long profileOwnerId = profile.getUserId();
        boolean isOwnProfile = profileOwnerId != null && profileOwnerId.equals(currentUserId);

        model.addAttribute("profile", profile);
        model.addAttribute("profileOwnerId", profileOwnerId);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("canLeaveComment", !isOwnProfile);
        model.addAttribute("commentCount", profileCommentService.countCommentsForProfile(profileOwnerId));
        model.addAttribute("comments", profileCommentService.getLatestCommentsForProfile(profileOwnerId, 20, currentUserId));

        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", new CreateProfileCommentDto());
        }
    }

    private Long extractCurrentUserId(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        if (principal instanceof OAuth2User oauth2User) {
            Object userId = oauth2User.getAttribute("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }

            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmail(email)
                        .map(user -> user.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
        }

        throw new RuntimeException("Unable to resolve current user id");
    }

}

