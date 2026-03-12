package com.example.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillSwapProposalCreateRequest {

    @NotNull
    private Long announceId;

    @Size(max = 500)
    private String message;
}
