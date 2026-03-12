package com.example.skillswap.admin.dto;

import java.time.LocalDateTime;

public record AdminReviewRowDto(
        Long id,
        String profileOwner,
        String author,
        String content,
        Integer rating,
        LocalDateTime createdAt,
        boolean reported
) {
}