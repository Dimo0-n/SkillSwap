package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillSwapProposalAvailabilityResponse {
    private boolean canSubmit;
    private String status;
    private String message;
    private String actionUrl;
    private String actionLabel;
}