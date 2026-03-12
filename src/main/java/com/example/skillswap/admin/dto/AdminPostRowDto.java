package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminPostRowDto(
        Long id,
        String title,
        String authorName,
        String creatorEmail,
        String offeredSkill,
        String requestedSkill,
        LocalDateTime createdAt,
        boolean spam,
        long relatedSessions
) {
}