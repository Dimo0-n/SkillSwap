package com.example.skillswap.controller;

import com.example.skillswap.dto.CreateSwapReviewRequest;
import com.example.skillswap.dto.SwapReviewDto;
import com.example.skillswap.exceptions.ApiException;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.ProfileCompletionService;
import com.example.skillswap.service.SwapReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/swaps/{proposalId}/reviews")
@RequiredArgsConstructor
public class SwapReviewController {

    private final SwapReviewService swapReviewService;
    private final ChatService chatService;
    private final ProfileCompletionService profileCompletionService;

    @PostMapping
    public ResponseEntity<?> createReview(@PathVariable Long proposalId,
                                          @Valid @RequestBody CreateSwapReviewRequest request,
                                          Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildAuthenticationRequiredResponse());
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildProfileCompletionRequiredResponse());
        }

        Long reviewerId = chatService.getCurrentUserId(principal);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(swapReviewService.createReview(proposalId, reviewerId, request));
        } catch (ApiException exception) {
            return ResponseEntity.status(exception.getStatus()).body(buildFailureResponse(exception.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getReviews(@PathVariable Long proposalId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildAuthenticationRequiredResponse());
        }

        Long currentUserId = chatService.getCurrentUserId(principal);
        try {
            List<SwapReviewDto> reviews = swapReviewService.getReviewsForProposal(proposalId, currentUserId);
            return ResponseEntity.ok(reviews);
        } catch (ApiException exception) {
            return ResponseEntity.status(exception.getStatus()).body(buildFailureResponse(exception.getMessage()));
        }
    }

    private Map<String, Object> buildFailureResponse(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }

    private Map<String, Object> buildProfileCompletionRequiredResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Complete your profile before using this feature.");
        body.put("redirectUrl", ProfileCompletionService.REQUIRED_REDIRECT_PATH);
        return body;
    }

    private Map<String, Object> buildAuthenticationRequiredResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Authentication is required before using this feature.");
        body.put("redirectUrl", "/login");
        return body;
    }
}
