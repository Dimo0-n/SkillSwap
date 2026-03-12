package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminRecentActionDto(
        String actorName,
        String description,
        LocalDateTime createdAt
) {
}