package com.example.skillswap.service;

import com.example.skillswap.dto.SkillSwapProposalAvailabilityResponse;
import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.SkillSwapProposalActionResponse;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AnnounceStatus;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import com.example.skillswap.exceptions.ApiException;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.SkillSwapProposalRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
@RequiredArgsConstructor
public class SkillSwapProposalService {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private static final EnumSet<SkillSwapProposalStatus> BLOCKING_STATUSES = EnumSet.of(
            SkillSwapProposalStatus.PENDING,
            SkillSwapProposalStatus.ACCEPTED,
            SkillSwapProposalStatus.IN_PROGRESS,
            SkillSwapProposalStatus.NEGOTIATING
        );

        private static final EnumSet<SkillSwapProposalStatus> ACTIVE_ANNOUNCEMENT_LOCK_STATUSES = EnumSet.of(
            SkillSwapProposalStatus.ACCEPTED,
            SkillSwapProposalStatus.IN_PROGRESS
        );

        private static final Map<SkillSwapProposalStatus, Set<SkillSwapProposalStatus>> ALLOWED_TRANSITIONS = Map.of(
            SkillSwapProposalStatus.PENDING, Set.of(SkillSwapProposalStatus.NEGOTIATING, SkillSwapProposalStatus.ACCEPTED, SkillSwapProposalStatus.CANCELLED),
            SkillSwapProposalStatus.ACCEPTED, Set.of(SkillSwapProposalStatus.IN_PROGRESS, SkillSwapProposalStatus.CANCELLED),
            SkillSwapProposalStatus.IN_PROGRESS, Set.of(SkillSwapProposalStatus.COMPLETED, SkillSwapProposalStatus.CANCELLED),
            SkillSwapProposalStatus.NEGOTIATING, Set.of(SkillSwapProposalStatus.ACCEPTED, SkillSwapProposalStatus.CANCELLED)
    );

    private final AnnounceRepository announceRepository;
    private final UserRepository userRepository;
    private final SkillSwapProposalRepository skillSwapProposalRepository;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageSource messageSource;

    @Transactional
    public SkillSwapProposal createProposal(Long requesterId, Long announceId, String requesterMessage) {
        Announce announce = announceRepository.findById(announceId)
                .orElseThrow(() -> new RuntimeException("Anuntul nu a fost gasit."));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit."));
        User owner = announce.getUser();

        if (owner == null) {
            throw new RuntimeException("Anuntul nu are un proprietar valid.");
        }
        if (owner.getId().equals(requesterId)) {
            throw new RuntimeException("Nu poti trimite o propunere propriului anunt.");
        }
        if (announce.getStatus() != AnnounceStatus.ACTIVE) {
            throw new RuntimeException("Acest anunt nu mai accepta propuneri noi.");
        }

        SkillSwapProposalAvailabilityResponse availability = getProposalAvailability(requesterId, announceId);
        if (!availability.isCanSubmit()) {
            throw new RuntimeException(availability.getMessage());
        }

        SkillSwapProposal proposal = new SkillSwapProposal();
        proposal.setAnnounce(announce);
        proposal.setRequester(requester);
        proposal.setOwner(owner);
        proposal.setOfferedSkill(normalizeRequiredText(announce.getCategoryOffered(), "Skill-ul oferit lipseste."));
        proposal.setRequestedSkill(normalizeRequiredText(announce.getCategoryRequired(), "Skill-ul cerut lipseste."));
        proposal.setRequesterMessage(normalizeOptionalMessage(requesterMessage));
        proposal.setStatus(SkillSwapProposalStatus.PENDING);

        SkillSwapProposal savedProposal = skillSwapProposalRepository.save(proposal);

        notificationService.createNotification(
                owner.getId(),
                NotificationType.SKILL_REQUEST,
                requester.getFullName() + " ti-a propus un Skill Swap",
                savedProposal.getOfferedSkill() + " ↔ " + savedProposal.getRequestedSkill(),
                "/announce-details/" + announce.getId(),
                savedProposal
        );

        return savedProposal;
    }

    @Transactional(readOnly = true)
    public SkillSwapProposalAvailabilityResponse getProposalAvailability(Long requesterId, Long announceId) {
        Announce announce = announceRepository.findById(announceId)
                .orElseThrow(() -> new RuntimeException("Anuntul nu a fost gasit."));
        User owner = announce.getUser();

        if (owner == null) {
            throw new RuntimeException("Anuntul nu are un proprietar valid.");
        }

        if (owner.getId().equals(requesterId)) {
            return new SkillSwapProposalAvailabilityResponse(
                    false,
                    null,
                    "Nu poti trimite o propunere propriului anunt.",
                    null,
                    null
            );
        }

        if (announce.getStatus() != AnnounceStatus.ACTIVE) {
            return new SkillSwapProposalAvailabilityResponse(
                false,
                null,
                "Anuntul nu mai este activ pentru propuneri noi.",
                null,
                null
            );
        }

        return skillSwapProposalRepository
                .findTopByAnnounceIdAndRequesterIdAndStatusInOrderByCreatedAtDesc(announceId, requesterId, BLOCKING_STATUSES)
                .map(this::buildUnavailableResponse)
                .orElseGet(() -> new SkillSwapProposalAvailabilityResponse(true, null, null, null, null));
    }

    @Transactional
    public SkillSwapProposalActionResponse acceptProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = getOwnerProposalForStatuses(
            proposalId,
            ownerId,
            EnumSet.of(SkillSwapProposalStatus.PENDING, SkillSwapProposalStatus.NEGOTIATING)
        );
        ensureNoOtherActiveSwapForAnnouncement(proposal);
        ChatRoom chatRoom = openProposalChat(proposal, SkillSwapProposalStatus.ACCEPTED);

        transitionStatus(proposal, SkillSwapProposalStatus.ACCEPTED);
        proposal.setRespondedAt(UtcDateTimes.now());
        proposal.setAcceptedAt(UtcDateTimes.now());
        proposal.setChatRoom(chatRoom);
        lockAnnouncementForActiveSwap(proposal);
        persistTransition(proposal);

        notificationService.markProposalNotificationsAsRead(ownerId, proposal.getId());
        notificationService.createNotification(
                proposal.getRequester().getId(),
                NotificationType.REQUEST_ACCEPTED,
                "Propunerea ta de Skill Swap a fost acceptata",
                proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill(),
                "/chat-history?roomId=" + chatRoom.getId(),
                proposal
        );

        return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Propunerea a fost acceptata. Conversatia este pregatita.",
                chatRoom.getId(),
                "/chat-history?roomId=" + chatRoom.getId()
        );
    }

    @Transactional
    public SkillSwapProposalActionResponse rejectProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = getOwnerProposalForStatuses(
            proposalId,
            ownerId,
            EnumSet.of(SkillSwapProposalStatus.PENDING, SkillSwapProposalStatus.ACCEPTED, SkillSwapProposalStatus.IN_PROGRESS, SkillSwapProposalStatus.NEGOTIATING)
        );

        transitionStatus(proposal, SkillSwapProposalStatus.CANCELLED);
        proposal.setRespondedAt(UtcDateTimes.now());
        proposal.setCancelledAt(UtcDateTimes.now());
        proposal.setCancelledByUserId(ownerId);
        proposal.setCancellationReason("Respinsa de proprietarul anuntului.");
        updateAnnouncementAfterCancellation(proposal);
        persistTransition(proposal);

        notificationService.markProposalNotificationsAsRead(ownerId, proposal.getId());
        notificationService.createNotification(
                proposal.getRequester().getId(),
                NotificationType.REQUEST_REJECTED,
                "Propunerea ta de Skill Swap a fost anulata",
                proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill(),
                "/profile/" + proposal.getOwner().getId(),
                proposal
        );

        return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Propunerea a fost anulata.",
                null,
                null
        );
    }

    @Transactional
    public SkillSwapProposalActionResponse negotiateProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = getOwnerProposalForStatuses(
            proposalId,
            ownerId,
            EnumSet.of(SkillSwapProposalStatus.PENDING, SkillSwapProposalStatus.NEGOTIATING)
        );

        ChatRoom chatRoom = openProposalChat(proposal, SkillSwapProposalStatus.NEGOTIATING);

        transitionStatus(proposal, SkillSwapProposalStatus.NEGOTIATING);
        proposal.setRespondedAt(UtcDateTimes.now());
        proposal.setChatRoom(chatRoom);
        persistTransition(proposal);

        notificationService.markProposalNotificationsAsRead(ownerId, proposal.getId());
        notificationService.createNotification(
            proposal.getRequester().getId(),
            NotificationType.REQUEST_NEGOTIATING,
            "Propunerea ta de Skill Swap este in negociere",
            proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill(),
            "/chat-history?roomId=" + chatRoom.getId(),
            proposal
        );

        return new SkillSwapProposalActionResponse(
            true,
            proposal.getStatus().name(),
            "Propunerea a fost mutata in negociere.",
            chatRoom.getId(),
            "/chat-history?roomId=" + chatRoom.getId()
        );
        }

            @Transactional
            public SkillSwapProposalActionResponse startProposal(Long proposalId, Long actorId) {
            SkillSwapProposal proposal = getParticipantProposalForStatuses(
                proposalId,
                actorId,
                EnumSet.of(SkillSwapProposalStatus.ACCEPTED)
            );

                ensureNoOtherActiveSwapForAnnouncement(proposal);
            transitionStatus(proposal, SkillSwapProposalStatus.IN_PROGRESS);
            proposal.setStartedAt(UtcDateTimes.now());
            lockAnnouncementForActiveSwap(proposal);
                persistTransition(proposal);

            return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Schimbul a fost pornit.",
                proposal.getChatRoom() != null ? proposal.getChatRoom().getId() : null,
                proposal.getChatRoom() != null ? "/chat-history?roomId=" + proposal.getChatRoom().getId() : null
            );
            }

            @Transactional
            public SkillSwapProposalActionResponse completeProposal(Long proposalId, Long actorId) {
            SkillSwapProposal proposal = getParticipantProposalForStatuses(
                proposalId,
                actorId,
                EnumSet.of(SkillSwapProposalStatus.IN_PROGRESS)
            );

            transitionStatus(proposal, SkillSwapProposalStatus.COMPLETED);
            proposal.setCompletedAt(UtcDateTimes.now());
            closeAnnouncementForCompletedSwap(proposal);
            persistTransition(proposal);

            return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Schimbul a fost marcat ca finalizat.",
                proposal.getChatRoom() != null ? proposal.getChatRoom().getId() : null,
                proposal.getChatRoom() != null ? "/chat-history?roomId=" + proposal.getChatRoom().getId() : null
            );
            }

            @Transactional
            public SkillSwapProposalActionResponse cancelProposal(Long proposalId, Long actorId) {
            SkillSwapProposal proposal = getParticipantProposalForStatuses(
                proposalId,
                actorId,
                EnumSet.of(SkillSwapProposalStatus.ACCEPTED, SkillSwapProposalStatus.IN_PROGRESS)
            );

            transitionStatus(proposal, SkillSwapProposalStatus.CANCELLED);
            proposal.setCancelledAt(UtcDateTimes.now());
            proposal.setCancelledByUserId(actorId);
            proposal.setCancellationReason("Anulata de unul dintre participanti.");
            updateAnnouncementAfterCancellation(proposal);
            persistTransition(proposal);

            return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Schimbul a fost anulat.",
                proposal.getChatRoom() != null ? proposal.getChatRoom().getId() : null,
                proposal.getChatRoom() != null ? "/chat-history?roomId=" + proposal.getChatRoom().getId() : null
            );
    }

    private SkillSwapProposal getOwnerProposalForStatuses(Long proposalId,
                                                          Long ownerId,
                                                          EnumSet<SkillSwapProposalStatus> allowedStatuses) {
        return skillSwapProposalRepository.findByIdAndOwnerIdAndStatusIn(proposalId, ownerId, allowedStatuses)
                .orElseThrow(() -> new RuntimeException("Propunerea nu a fost gasita."));
    }

    private SkillSwapProposal getParticipantProposalForStatuses(Long proposalId,
                                                                Long userId,
                                                                EnumSet<SkillSwapProposalStatus> allowedStatuses) {
        SkillSwapProposal proposal = skillSwapProposalRepository.findByIdForUpdate(proposalId)
                .orElseThrow(() -> new RuntimeException("Propunerea nu a fost gasita."));

        boolean participant = proposal.getOwner() != null && proposal.getOwner().getId().equals(userId)
                || proposal.getRequester() != null && proposal.getRequester().getId().equals(userId);
        if (!participant) {
            throw new RuntimeException("Nu ai permisiunea pentru aceasta actiune.");
        }

        if (!allowedStatuses.contains(proposal.getStatus())) {
            throw new RuntimeException("Propunerea nu mai este disponibila pentru aceasta actiune.");
        }

        return proposal;
    }

    private void transitionStatus(SkillSwapProposal proposal, SkillSwapProposalStatus targetStatus) {
        SkillSwapProposalStatus currentStatus = proposal.getStatus();
        if (currentStatus == targetStatus) {
            return;
        }

        Set<SkillSwapProposalStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new ApiException(HttpStatus.CONFLICT, "Tranzitie invalida de stare pentru Skill Swap.");
        }

        proposal.setStatus(targetStatus);
    }

    private void ensureNoOtherActiveSwapForAnnouncement(SkillSwapProposal proposal) {
        boolean hasAnotherActiveSwap = skillSwapProposalRepository.existsByAnnounceIdAndStatusInAndIdNot(
                proposal.getAnnounce().getId(),
                ACTIVE_ANNOUNCEMENT_LOCK_STATUSES,
                proposal.getId()
        );
        if (hasAnotherActiveSwap) {
            throw new ApiException(HttpStatus.CONFLICT, "Anuntul este deja blocat de un alt schimb activ.");
        }
    }

    private void persistTransition(SkillSwapProposal proposal) {
        try {
            skillSwapProposalRepository.saveAndFlush(proposal);
        } catch (OptimisticLockingFailureException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Starea schimbului a fost actualizata de alt utilizator. Reincarca pagina.");
        }
    }

    private void lockAnnouncementForActiveSwap(SkillSwapProposal proposal) {
        Announce announce = proposal.getAnnounce();
        announce.setStatus(AnnounceStatus.INACTIVE);
        announce.setLockedBySwap(proposal);
        announce.setInactivatedReason("SWAP_ACCEPTED");
        announce.setInactivatedAt(UtcDateTimes.now());
    }

    private void closeAnnouncementForCompletedSwap(SkillSwapProposal proposal) {
        Announce announce = proposal.getAnnounce();
        announce.setStatus(AnnounceStatus.CLOSED);
        announce.setLockedBySwap(proposal);
        announce.setInactivatedReason("SWAP_COMPLETED");
        announce.setInactivatedAt(UtcDateTimes.now());
    }

    private void updateAnnouncementAfterCancellation(SkillSwapProposal proposal) {
        Announce announce = proposal.getAnnounce();
        boolean hasAnotherActiveSwap = skillSwapProposalRepository.existsByAnnounceIdAndStatusInAndIdNot(
                announce.getId(),
                ACTIVE_ANNOUNCEMENT_LOCK_STATUSES,
                proposal.getId()
        );

        if (hasAnotherActiveSwap) {
            return;
        }

        announce.setStatus(AnnounceStatus.ACTIVE);
        announce.setLockedBySwap(null);
        announce.setInactivatedReason(null);
        announce.setInactivatedAt(null);
    }

        private ChatRoom openProposalChat(SkillSwapProposal proposal, SkillSwapProposalStatus status) {
        ChatRoom chatRoom = chatService.createOrGetChatRoom(
                proposal.getRequester().getId(),
                proposal.getOwner().getId()
        );

        String statusLabel = mapProposalStatusLabel(status);
        String systemTitle = resolveMessage("chat.proposal.system.title", "Skill Swap Proposal");
        String exchangeSummary = proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill();
        String previewContent = systemTitle + ": " + exchangeSummary + " (" + statusLabel + ")";
        ChatMessageDTO systemMessage = chatService.createSystemMessage(
                chatRoom.getId(),
                proposal.getOwner().getId(),
                previewContent,
            systemTitle,
                exchangeSummary,
                statusLabel
        );

        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), systemMessage);
        return chatRoom;
    }

    private String mapProposalStatusLabel(SkillSwapProposalStatus status) {
        String key = switch (status) {
            case PENDING -> "chat.proposal.status.pending";
            case NEGOTIATING -> "chat.proposal.status.negotiating";
            case ACCEPTED -> "chat.proposal.status.accepted";
            case IN_PROGRESS -> "chat.proposal.status.inProgress";
            case COMPLETED -> "chat.proposal.status.completed";
            case CANCELLED -> "chat.proposal.status.cancelled";
            case REJECTED -> "chat.proposal.status.rejected";
        };
        return resolveMessage(key, status.name());
    }

    private String resolveMessage(String key, String fallback) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, fallback, locale);
    }

    private SkillSwapProposalAvailabilityResponse buildUnavailableResponse(SkillSwapProposal proposal) {
        SkillSwapProposalStatus status = proposal.getStatus();
        if (status == SkillSwapProposalStatus.IN_PROGRESS) {
            return new SkillSwapProposalAvailabilityResponse(
                    false,
                    status.name(),
                    "Ai deja un schimb in desfasurare pentru acest anunt.",
                    proposal.getChatRoom() != null ? "/chat-history?roomId=" + proposal.getChatRoom().getId() : null,
                    proposal.getChatRoom() != null ? "Deschide conversatia" : null
            );
        }

        if (status == SkillSwapProposalStatus.ACCEPTED) {
            return new SkillSwapProposalAvailabilityResponse(
                    false,
                    status.name(),
                    "Acest Skill Swap a fost deja acceptat.",
                    proposal.getChatRoom() != null ? "/chat-history?roomId=" + proposal.getChatRoom().getId() : null,
                    proposal.getChatRoom() != null ? "Mergi la chat" : null
            );
        }

        if (status == SkillSwapProposalStatus.COMPLETED || status == SkillSwapProposalStatus.CANCELLED) {
            return new SkillSwapProposalAvailabilityResponse(
                    false,
                    status.name(),
                    "Acest anunt nu mai este disponibil pentru o noua propunere.",
                    null,
                    null
            );
        }

        return new SkillSwapProposalAvailabilityResponse(
                false,
                status.name(),
                "Ai deja o cerere trimisa pentru acest anunt. Asteapta raspunsul proprietarului.",
                null,
                null
        );
    }

    private String normalizeRequiredText(String rawValue, String errorMessage) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new RuntimeException(errorMessage);
        }

        return rawValue.trim();
    }

    private String normalizeOptionalMessage(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String normalized = rawValue.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new RuntimeException("Mesajul propunerii poate avea maximum 500 de caractere.");
        }

        return normalized;
    }
}
