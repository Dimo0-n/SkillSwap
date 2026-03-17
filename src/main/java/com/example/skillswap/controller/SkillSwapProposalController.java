package com.example.skillswap.controller;

import com.example.skillswap.dto.SkillSwapProposalActionResponse;
import com.example.skillswap.dto.SkillSwapProposalAvailabilityResponse;
import com.example.skillswap.dto.SkillSwapProposalCreateRequest;
import com.example.skillswap.exceptions.ApiException;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.ProfileCompletionService;
import com.example.skillswap.service.SkillSwapProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/skill-swap-proposals")
@RequiredArgsConstructor
public class SkillSwapProposalController {

    private final SkillSwapProposalService skillSwapProposalService;
    private final ChatService chatService;
    private final ProfileCompletionService profileCompletionService;

    @GetMapping("/availability")
    public ResponseEntity<?> getProposalAvailability(@RequestParam Long announceId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildAuthenticationRequiredResponse());
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildProfileCompletionRequiredResponse());
        }

        Long requesterId = chatService.getCurrentUserId(principal);
        SkillSwapProposalAvailabilityResponse response = skillSwapProposalService.getProposalAvailability(requesterId, announceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProposal(@Valid @RequestBody SkillSwapProposalCreateRequest request,
                                                              Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildAuthenticationRequiredResponse());
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildProfileCompletionRequiredResponse());
        }

        try {
            Long requesterId = chatService.getCurrentUserId(principal);
            skillSwapProposalService.createProposal(requesterId, request.getAnnounceId(), request.getMessage());

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "Propunerea a fost trimisa.");
            return ResponseEntity.ok(body);
        } catch (Exception exception) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", exception.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @PostMapping("/{proposalId}/accept")
    public ResponseEntity<SkillSwapProposalActionResponse> acceptProposal(@PathVariable Long proposalId,
                                                                          Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::acceptProposal);
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<SkillSwapProposalActionResponse> rejectProposal(@PathVariable Long proposalId,
                                                                          Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::rejectProposal);
    }

    @PostMapping("/{proposalId}/negotiate")
    public ResponseEntity<SkillSwapProposalActionResponse> negotiateProposal(@PathVariable Long proposalId,
                                                                             Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::negotiateProposal);
    }

    @PostMapping("/{proposalId}/start")
    public ResponseEntity<SkillSwapProposalActionResponse> startProposal(@PathVariable Long proposalId,
                                                                         Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::startProposal);
    }

    @PostMapping("/{proposalId}/complete")
    public ResponseEntity<SkillSwapProposalActionResponse> completeProposal(@PathVariable Long proposalId,
                                                                            Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::completeProposal);
    }

    @PostMapping("/{proposalId}/cancel")
    public ResponseEntity<SkillSwapProposalActionResponse> cancelProposal(@PathVariable Long proposalId,
                                                                          Principal principal) {
        return handleDecision(proposalId, principal, skillSwapProposalService::cancelProposal);
    }

    private ResponseEntity<SkillSwapProposalActionResponse> handleDecision(Long proposalId,
                                                                           Principal principal,
                                                                           ProposalDecisionHandler handler) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SkillSwapProposalActionResponse(false, null, "Autentificarea este necesara.", null, "/login"));
        }

        try {
            Long actorId = chatService.getCurrentUserId(principal);
            return ResponseEntity.ok(handler.handle(proposalId, actorId));
        } catch (ApiException exception) {
            return ResponseEntity.status(exception.getStatus())
                    .body(new SkillSwapProposalActionResponse(false, null, exception.getMessage(), null, null));
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(new SkillSwapProposalActionResponse(false, null, exception.getMessage(), null, null));
        }
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

    @FunctionalInterface
    private interface ProposalDecisionHandler {
        SkillSwapProposalActionResponse handle(Long proposalId, Long ownerId);
    }
}
