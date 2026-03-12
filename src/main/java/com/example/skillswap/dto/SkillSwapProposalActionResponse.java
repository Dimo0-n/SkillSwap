package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillSwapProposalActionResponse {
    private boolean success;
    private String status;
    private String message;
    private Long chatRoomId;
    private String redirectUrl;
}
