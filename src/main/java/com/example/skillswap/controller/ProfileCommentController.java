package com.example.skillswap.controller;

import com.example.skillswap.dto.CreateProfileCommentDto;
import com.example.skillswap.service.ProfileCommentService;
import com.example.skillswap.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile/{profileOwnerId}/comments")
@RequiredArgsConstructor
public class ProfileCommentController {

    private final ProfileCommentService profileCommentService;
    private final ChatService chatService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String createComment(@PathVariable Long profileOwnerId,
                                @Valid @ModelAttribute("commentForm") CreateProfileCommentDto commentForm,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        Long currentUserId = chatService.getCurrentUserId(authentication);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentForm", commentForm);
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.commentForm",
                    bindingResult
            );
            return "redirect:" + buildRedirectPath(profileOwnerId, currentUserId);
        }

        try {
            profileCommentService.createComment(profileOwnerId, currentUserId, commentForm);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comentariul a fost adaugat.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("commentForm", commentForm);
            redirectAttributes.addFlashAttribute("commentError", exception.getMessage());
        }

        return "redirect:" + buildRedirectPath(profileOwnerId, currentUserId);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Long profileOwnerId,
                                @PathVariable Long commentId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        Long currentUserId = chatService.getCurrentUserId(authentication);

        try {
            profileCommentService.deleteComment(profileOwnerId, commentId, currentUserId);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comentariul a fost sters.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("commentError", exception.getMessage());
        }

        return "redirect:" + buildRedirectPath(profileOwnerId, currentUserId);
    }

    private String buildRedirectPath(Long profileOwnerId, Long currentUserId) {
        if (profileOwnerId.equals(currentUserId)) {
            return "/profile";
        }
        return "/profile/" + profileOwnerId;
    }
}
