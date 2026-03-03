package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoSessionReadyDto {
    private final String message;
    private final String meetingUrl;
    private final Long createdByUserId;
}
