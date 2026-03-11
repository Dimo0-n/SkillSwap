package com.example.skillswap.service.impl;

import com.example.skillswap.dto.CreateProfileCommentDto;
import com.example.skillswap.dto.ProfileCommentDto;
import com.example.skillswap.entity.ProfileComment;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.repository.ProfileCommentRepository;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfileCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileCommentServiceImpl implements ProfileCommentService {

    private static final int DEFAULT_COMMENT_PAGE_SIZE = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MAX_COMMENT_LENGTH = 200;

    private final ProfileCommentRepository profileCommentRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final com.example.skillswap.service.NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<ProfileCommentDto> getLatestCommentsForProfile(Long profileOwnerId, int limit, Long currentUserId) {
        int requestedLimit = limit <= 0 ? DEFAULT_COMMENT_PAGE_SIZE : limit;
        int boundedLimit = Math.max(1, Math.min(requestedLimit, MAX_LIMIT));

        return profileCommentRepository
                .findByProfileOwnerIdOrderByCreatedAtDescIdDesc(profileOwnerId, PageRequest.of(0, boundedLimit))
                .stream()
                .map(comment -> toDto(comment, profileOwnerId, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCommentsForProfile(Long profileOwnerId) {
        return profileCommentRepository.countByProfileOwnerId(profileOwnerId);
    }

    @Override
    @Transactional
    public ProfileCommentDto createComment(Long profileOwnerId, Long authorId, CreateProfileCommentDto dto) {
        User profileOwner = userRepository.findById(profileOwnerId)
                .orElseThrow(() -> new RuntimeException("Profilul nu exista."));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Autorul comentariului nu exista."));

        if (profileOwner.getId().equals(author.getId())) {
            throw new RuntimeException("Nu poti adauga un comentariu pe propriul profil.");
        }

        String normalizedContent = normalizeContent(dto.getContent());
        if (normalizedContent.isEmpty()) {
            throw new RuntimeException("Comentariul nu poate fi gol.");
        }
        if (normalizedContent.length() > MAX_COMMENT_LENGTH) {
            throw new RuntimeException("Comentariul poate avea maximum 200 de caractere.");
        }

        ProfileComment comment = new ProfileComment();
        comment.setProfileOwner(profileOwner);
        comment.setAuthor(author);
        comment.setAuthorDisplayName(resolveAuthorDisplayName(author));
        comment.setContent(normalizedContent);

        ProfileComment saved = profileCommentRepository.save(comment);

        notificationService.createNotification(
                profileOwner.getId(),
                NotificationType.NEW_REVIEW,
                "Comentariu nou pe profil",
                saved.getAuthorDisplayName() + " a lasat un comentariu pe profilul tau.",
                "/profile"
        );

        return toDto(saved, profileOwnerId, authorId);
    }

    @Override
    @Transactional
    public void deleteComment(Long profileOwnerId, Long commentId, Long currentUserId) {
        ProfileComment comment = profileCommentRepository.findDetailedByIdAndProfileOwnerId(commentId, profileOwnerId)
                .orElseThrow(() -> new RuntimeException("Comentariul nu a fost gasit."));

        boolean canDelete = comment.getAuthor().getId().equals(currentUserId);

        if (!canDelete) {
            throw new RuntimeException("Nu ai permisiunea sa stergi acest comentariu.");
        }

        profileCommentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void reportComment(Long profileOwnerId, Long commentId, Long currentUserId) {
        ProfileComment comment = profileCommentRepository.findDetailedByIdAndProfileOwnerId(commentId, profileOwnerId)
                .orElseThrow(() -> new RuntimeException("Comentariul nu a fost gasit."));

        if (!comment.getProfileOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("Doar proprietarul profilului poate raporta acest comentariu.");
        }

        if (comment.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("Nu poti raporta propriul comentariu.");
        }

        if (comment.isReported()) {
            throw new RuntimeException("Comentariul a fost deja raportat.");
        }

        comment.setReported(true);
        comment.setReportedAt(LocalDateTime.now());
    }

    private ProfileCommentDto toDto(ProfileComment comment, Long profileOwnerId, Long currentUserId) {
        boolean isAuthor = currentUserId != null && comment.getAuthor().getId().equals(currentUserId);
        boolean isProfileOwner = currentUserId != null && profileOwnerId.equals(currentUserId);
        boolean canDelete = isAuthor;
        boolean canReport = isProfileOwner && !isAuthor && !comment.isReported();

        return new ProfileCommentDto(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthorDisplayName(),
                "/profile/image/" + comment.getAuthor().getEmail(),
                comment.getContent(),
                comment.getCreatedAt(),
                canDelete,
                canReport,
                comment.isReported()
        );
    }

    private String resolveAuthorDisplayName(User author) {
        return profileRepository.findFirstByUserIdOrderByIdDesc(author.getId())
                .map(Profil::getName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(author.getFullName());
    }

    private String normalizeContent(String rawContent) {
        if (rawContent == null) {
            return "";
        }

        return rawContent.replace("\r\n", "\n").trim();
    }
}
