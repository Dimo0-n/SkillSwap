package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ConversationSummaryDTO {
    private Long chatRoomId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private String lastMessage;
    private Instant lastMessageTime;
    private Long unreadCount;
    private Boolean otherUserOnline;
    private Instant otherUserLastSeenAt;
    private String otherUserTimeZoneId;
    private Long activeProposalId;
    private String activeProposalStatus;
    private String activeProposalStatusLabel;
    private Boolean currentUserIsProposalOwner;
    private Boolean canAcceptActiveProposal;
}
