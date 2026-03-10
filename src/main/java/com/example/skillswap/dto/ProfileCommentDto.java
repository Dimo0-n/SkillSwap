package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProfileCommentDto {

    private final Long id;
    private final Long authorId;
    private final String authorName;
    private final String authorImageUrl;
    private final String content;
    private final LocalDateTime createdAt;
    private final boolean canDelete;
}
