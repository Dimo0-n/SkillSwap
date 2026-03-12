package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationProposalDTO {
    private Long proposalId;
    private String status;
    private String statusLabel;
    private Long actorUserId;
    private String actorName;
    private String actorAvatarUrl;
    private String offeredSkill;
    private String requestedSkill;
    private String requesterMessage;
    private boolean actionable;
    private String viewProfileUrl;
    private String chatUrl;
    private Long announceId;
    private String announceTitle;
}
