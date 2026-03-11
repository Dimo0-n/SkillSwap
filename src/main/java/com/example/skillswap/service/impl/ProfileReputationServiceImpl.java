package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ProfileReputationResult;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.repository.ProfileCommentRepository;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.service.AiService;
import com.example.skillswap.service.ProfileReputationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileReputationServiceImpl implements ProfileReputationService {

    private static final Logger log = LoggerFactory.getLogger(ProfileReputationServiceImpl.class);

    private static final int MINIMUM_FEEDBACK_COMMENTS = 4;
    private static final int REGENERATION_INTERVAL = 10;

    private final ProfileRepository profileRepository;
    private final ProfileCommentRepository profileCommentRepository;
    private final AiService aiService;

    @Override
    @Transactional
    public void refreshProfileReputationIfNeeded(Long profileOwnerId) {
        Profil profile = profileRepository.findFirstByUserIdOrderByIdDesc(profileOwnerId)
                .orElse(null);

        if (profile == null) {
            log.debug("Skipping AI reputation refresh because profileOwnerId={} has no profile row yet", profileOwnerId);
            return;
        }

        int totalComments = Math.toIntExact(profileCommentRepository.countByProfileOwnerId(profileOwnerId));
        if (totalComments < MINIMUM_FEEDBACK_COMMENTS) {
            clearEvaluationIfNeeded(profile);
            return;
        }

        if (!shouldGenerateEvaluation(profile, totalComments)) {
            return;
        }

        List<String> feedbackComments = profileCommentRepository.findContentsByProfileOwnerId(profileOwnerId);
        if (feedbackComments.size() < MINIMUM_FEEDBACK_COMMENTS) {
            clearEvaluationIfNeeded(profile);
            return;
        }

        try {
            ProfileReputationResult result = aiService.evaluateProfileReputation(feedbackComments);
            profile.setReputationScore(result.score());
            profile.setReputationSummary(result.summary());
            profile.setFeedbackCountAtLastEvaluation(totalComments);
            profileRepository.save(profile);
        } catch (Exception exception) {
            log.error("Could not refresh AI reputation for profileOwnerId={}", profileOwnerId, exception);
        }
    }

    private boolean shouldGenerateEvaluation(Profil profile, int totalComments) {
        int lastEvaluationCount = profile.getFeedbackCountAtLastEvaluation() == null
                ? 0
                : profile.getFeedbackCountAtLastEvaluation();

        if (lastEvaluationCount == 0) {
            return true;
        }

        return totalComments >= REGENERATION_INTERVAL
                && totalComments % REGENERATION_INTERVAL == 0
                && totalComments > lastEvaluationCount;
    }

    private void clearEvaluationIfNeeded(Profil profile) {
        boolean changed = profile.getReputationScore() != null
                || (profile.getReputationSummary() != null && !profile.getReputationSummary().isBlank())
                || (profile.getFeedbackCountAtLastEvaluation() != null && profile.getFeedbackCountAtLastEvaluation() != 0);

        if (!changed) {
            return;
        }

        profile.setReputationScore(null);
        profile.setReputationSummary(null);
        profile.setFeedbackCountAtLastEvaluation(0);
        profileRepository.save(profile);
    }
}
