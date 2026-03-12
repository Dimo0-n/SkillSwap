package com.example.skillswap.service;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.SkillSwapProposalActionResponse;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.SkillSwapProposalRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillSwapProposalService {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private final AnnounceRepository announceRepository;
    private final UserRepository userRepository;
    private final SkillSwapProposalRepository skillSwapProposalRepository;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

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
        if (skillSwapProposalRepository.existsByAnnounceIdAndRequesterIdAndStatus(
                announceId,
                requesterId,
                SkillSwapProposalStatus.PENDING
        )) {
            throw new RuntimeException("Ai deja o propunere activa pentru acest anunt.");
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

    @Transactional
    public SkillSwapProposalActionResponse acceptProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = getPendingOwnerProposal(proposalId, ownerId);
        ChatRoom chatRoom = openProposalChat(proposal, "Acceptat");

        proposal.setStatus(SkillSwapProposalStatus.ACCEPTED);
        proposal.setRespondedAt(UtcDateTimes.now());
        proposal.setChatRoom(chatRoom);

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
        SkillSwapProposal proposal = getPendingOwnerProposal(proposalId, ownerId);

        proposal.setStatus(SkillSwapProposalStatus.REJECTED);
        proposal.setRespondedAt(UtcDateTimes.now());

        notificationService.markProposalNotificationsAsRead(ownerId, proposal.getId());
        notificationService.createNotification(
                proposal.getRequester().getId(),
                NotificationType.REQUEST_REJECTED,
                "Propunerea ta de Skill Swap a fost refuzata",
                proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill(),
                "/profile/" + proposal.getOwner().getId(),
                proposal
        );

        return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Propunerea a fost refuzata.",
                null,
                null
        );
    }

    @Transactional
    public SkillSwapProposalActionResponse negotiateProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = getPendingOwnerProposal(proposalId, ownerId);
        ChatRoom chatRoom = openProposalChat(proposal, "In negociere");

        proposal.setStatus(SkillSwapProposalStatus.NEGOTIATING);
        proposal.setRespondedAt(UtcDateTimes.now());
        proposal.setChatRoom(chatRoom);

        notificationService.markProposalNotificationsAsRead(ownerId, proposal.getId());
        notificationService.createNotification(
                proposal.getRequester().getId(),
                NotificationType.REQUEST_NEGOTIATING,
                "Propunerea ta de Skill Swap a intrat in negociere",
                proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill(),
                "/chat-history?roomId=" + chatRoom.getId(),
                proposal
        );

        return new SkillSwapProposalActionResponse(
                true,
                proposal.getStatus().name(),
                "Conversația a fost deschisa pentru negociere.",
                chatRoom.getId(),
                "/chat-history?roomId=" + chatRoom.getId()
        );
    }

    private SkillSwapProposal getPendingOwnerProposal(Long proposalId, Long ownerId) {
        SkillSwapProposal proposal = skillSwapProposalRepository.findByIdAndOwnerId(proposalId, ownerId)
                .orElseThrow(() -> new RuntimeException("Propunerea nu a fost gasita."));

        if (proposal.getStatus() != SkillSwapProposalStatus.PENDING) {
            throw new RuntimeException("Propunerea nu mai este disponibila pentru aceasta actiune.");
        }

        return proposal;
    }

    private ChatRoom openProposalChat(SkillSwapProposal proposal, String statusLabel) {
        ChatRoom chatRoom = chatService.createOrGetChatRoom(
                proposal.getRequester().getId(),
                proposal.getOwner().getId()
        );

        String exchangeSummary = proposal.getOfferedSkill() + " ↔ " + proposal.getRequestedSkill();
        String previewContent = "Skill Swap: " + exchangeSummary + " (" + statusLabel + ")";
        ChatMessageDTO systemMessage = chatService.createSystemMessage(
                chatRoom.getId(),
                proposal.getOwner().getId(),
                previewContent,
                "Skill Swap Proposal",
                exchangeSummary,
                statusLabel
        );

        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), systemMessage);
        return chatRoom;
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
