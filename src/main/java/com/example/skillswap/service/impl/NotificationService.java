package com.example.skillswap.service.impl;

import com.example.skillswap.dto.NotificationDTO;
import com.example.skillswap.dto.NotificationProposalDTO;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.repository.NotificationRepository;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements com.example.skillswap.service.NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO createNotification(Long recipientUserId,
                                              NotificationType type,
                                              String title,
                                              String message,
                                              String targetUrl) {
        return createNotification(recipientUserId, type, title, message, targetUrl, null);
    }

    @Transactional
    public NotificationDTO createNotification(Long recipientUserId,
                                              NotificationType type,
                                              String title,
                                              String message,
                                              String targetUrl,
                                              SkillSwapProposal skillSwapProposal) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        notification.setSkillSwapProposal(skillSwapProposal);

        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = toDto(saved);

        // Private real-time event for the recipient.
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", dto);
        return dto;
    }

    @Transactional
    public NotificationDTO createWelcomeNotification(Long recipientUserId) {
        return createNotification(
                recipientUserId,
                NotificationType.WELCOME,
                "Bine ai venit pe SkillSwap",
                "Iti multumim ca te-ai alaturat comunitatii SkillSwap.",
                "/profile/complete"
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(Long userId, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, boundedLimit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCountForUser(Long userId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElse(null);
        if (notification == null) {
            return false;
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(UtcDateTimes.now());
        }

        return true;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadAtIsNull(userId);
        if (unread.isEmpty()) {
            return 0;
        }

        LocalDateTime now = UtcDateTimes.now();
        unread.forEach(item -> item.setReadAt(now));
        return unread.size();
    }

    @Transactional
    public void markProposalNotificationsAsRead(Long recipientUserId, Long proposalId) {
        if (proposalId == null) {
            return;
        }

        LocalDateTime now = UtcDateTimes.now();
        notificationRepository.findByRecipientIdAndSkillSwapProposalIdAndReadAtIsNull(recipientUserId, proposalId)
                .forEach(notification -> notification.setReadAt(now));
    }

        @Override
        @Transactional
        public int broadcastSystemNotification(String title, String message, String targetUrl) {
        List<User> recipients = userRepository.findAll().stream()
            .filter(user -> !user.isDeleted() && !user.isBanned())
            .toList();

        recipients.forEach(user -> createNotification(
            user.getId(),
            NotificationType.SYSTEM,
            title,
            message,
            targetUrl
        ));

        return recipients.size();
        }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = NotificationDTO.basic(notification);
        SkillSwapProposal proposal = notification.getSkillSwapProposal();
        if (proposal == null) {
            return dto;
        }

        User actor = resolveActor(notification, proposal);
        String actorAvatarUrl = resolveActorAvatarUrl(actor);
        boolean actionable = proposal.getStatus() == com.example.skillswap.enums.SkillSwapProposalStatus.PENDING
                && proposal.getOwner().getId().equals(notification.getRecipient().getId());
        String chatUrl = proposal.getChatRoom() != null
                ? "/chat-history?roomId=" + proposal.getChatRoom().getId()
                : null;

        dto.setProposal(new NotificationProposalDTO(
                proposal.getId(),
                proposal.getStatus().name(),
                mapStatusLabel(proposal.getStatus()),
                actor.getId(),
                actor.getFullName(),
                actorAvatarUrl,
                proposal.getOfferedSkill(),
                proposal.getRequestedSkill(),
                proposal.getRequesterMessage(),
                actionable,
                "/profile/" + actor.getId(),
                chatUrl,
                proposal.getAnnounce().getId(),
                proposal.getAnnounce().getTitle()
        ));
        return dto;
    }

    private User resolveActor(Notification notification, SkillSwapProposal proposal) {
        Long recipientId = notification.getRecipient().getId();
        if (proposal.getOwner().getId().equals(recipientId)) {
            return proposal.getRequester();
        }

        return proposal.getOwner();
    }

    private String resolveActorAvatarUrl(User actor) {
        return profileRepository.findFirstByUserIdOrderByIdDesc(actor.getId())
                .map(profile -> profile.getImageUrl())
                .filter(url -> url != null && !url.isBlank())
                .orElse("/img/default-avatar.png");
    }

    private String mapStatusLabel(com.example.skillswap.enums.SkillSwapProposalStatus status) {
        return switch (status) {
            case PENDING -> "In asteptare";
            case ACCEPTED -> "Acceptat";
            case IN_PROGRESS -> "In progres";
            case COMPLETED -> "Finalizat";
            case CANCELLED -> "Anulat";
            case REJECTED -> "Refuzat";
            case NEGOTIATING -> "In negociere";
        };
    }
}
