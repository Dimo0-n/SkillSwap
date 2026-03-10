package com.example.skillswap.service;

import com.example.skillswap.dto.CreateProfileCommentDto;
import com.example.skillswap.dto.ProfileCommentDto;

import java.util.List;

public interface ProfileCommentService {

    List<ProfileCommentDto> getLatestCommentsForProfile(Long profileOwnerId, int limit, Long currentUserId);

    long countCommentsForProfile(Long profileOwnerId);

    ProfileCommentDto createComment(Long profileOwnerId, Long authorId, CreateProfileCommentDto dto);

    void deleteComment(Long profileOwnerId, Long commentId, Long currentUserId);
}
