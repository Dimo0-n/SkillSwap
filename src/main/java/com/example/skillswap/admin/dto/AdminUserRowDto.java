package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminUserRowDto(
        Long id,
        String fullName,
        String email,
        String roles,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        long completedSessions,
        Double reputation,
        boolean suspended,
        boolean banned,
        boolean deleted
) {
}