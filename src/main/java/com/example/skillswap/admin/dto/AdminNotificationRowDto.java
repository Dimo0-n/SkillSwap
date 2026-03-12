package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminNotificationRowDto(
        Long id,
        String title,
        String message,
        String targetUrl,
        LocalDateTime createdAt
) {
}