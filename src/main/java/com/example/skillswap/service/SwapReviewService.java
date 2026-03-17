package com.example.skillswap.service;

import com.example.skillswap.dto.CreateSwapReviewRequest;
import com.example.skillswap.dto.SwapReviewDto;
import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.entity.SwapReview;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import com.example.skillswap.exceptions.ApiException;
import com.example.skillswap.repository.SkillSwapProposalRepository;
import com.example.skillswap.repository.SwapReviewRepository;
import com.example.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SwapReviewService {

    private final SwapReviewRepository swapReviewRepository;
    private final SkillSwapProposalRepository skillSwapProposalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public SwapReviewDto createReview(Long proposalId, Long reviewerId, CreateSwapReviewRequest request) {
        SkillSwapProposal proposal = skillSwapProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Schimbul nu a fost gasit."));

        if (proposal.getStatus() != SkillSwapProposalStatus.COMPLETED) {
            throw new ApiException(HttpStatus.CONFLICT, "Review-ul poate fi lasat doar dupa finalizarea schimbului.");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilizatorul nu a fost gasit."));

        boolean reviewerIsOwner = proposal.getOwner() != null && proposal.getOwner().getId().equals(reviewerId);
        boolean reviewerIsRequester = proposal.getRequester() != null && proposal.getRequester().getId().equals(reviewerId);
        if (!reviewerIsOwner && !reviewerIsRequester) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Nu ai acces sa lasi review pentru acest schimb.");
        }

        if (swapReviewRepository.existsByProposalIdAndReviewerId(proposalId, reviewerId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ai lasat deja un review pentru acest schimb.");
        }

        User reviewee = reviewerIsOwner ? proposal.getRequester() : proposal.getOwner();

        SwapReview review = new SwapReview();
        review.setProposal(proposal);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        SwapReview saved = swapReviewRepository.save(review);

        notificationService.createNotification(
                reviewee.getId(),
                NotificationType.NEW_REVIEW,
                "Review nou dupa Skill Swap",
                reviewer.getFullName() + " ti-a lasat un review.",
                "/chat-history?roomId=" + (proposal.getChatRoom() != null ? proposal.getChatRoom().getId() : "")
        );

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SwapReviewDto> getReviewsForProposal(Long proposalId, Long currentUserId) {
        SkillSwapProposal proposal = skillSwapProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Schimbul nu a fost gasit."));

        boolean currentUserIsParticipant = proposal.getOwner() != null && proposal.getOwner().getId().equals(currentUserId)
                || proposal.getRequester() != null && proposal.getRequester().getId().equals(currentUserId);
        if (!currentUserIsParticipant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Nu ai acces la review-urile acestui schimb.");
        }

        return swapReviewRepository.findByProposalIdOrderByCreatedAtAsc(proposalId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private SwapReviewDto toDto(SwapReview review) {
        return new SwapReviewDto(
                review.getId(),
                review.getProposal().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getFullName(),
                review.getReviewee().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private String normalizeComment(String rawComment) {
        if (rawComment == null) {
            return null;
        }

        String normalized = rawComment.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
