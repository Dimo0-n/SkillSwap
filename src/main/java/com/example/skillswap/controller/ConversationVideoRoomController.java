package com.example.skillswap.controller;

import com.example.skillswap.dto.VideoRoomResponseDto;
import com.example.skillswap.service.VideoRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationVideoRoomController {

    private final VideoRoomService videoRoomService;

    @PostMapping("/{id}/video-room")
    public ResponseEntity<VideoRoomResponseDto> getOrCreateVideoRoom(
            @PathVariable("id") Long conversationId,
            Authentication authentication) {
        VideoRoomResponseDto response = videoRoomService.getOrCreateVideoRoom(conversationId, authentication);
        return ResponseEntity.ok(response);
    }
}
