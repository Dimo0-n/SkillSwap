package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminReportRowDto(
        Long id,
        String reporter,
        String target,
        String reason,
        String targetType,
        String status,
        LocalDateTime createdAt
) {
}