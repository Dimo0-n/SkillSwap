package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class VideoRoomResponseDto {
    private final String meetingUrl;
    private final String meetingCode;
    private final Instant createdAt;
    private final boolean reused;
}
