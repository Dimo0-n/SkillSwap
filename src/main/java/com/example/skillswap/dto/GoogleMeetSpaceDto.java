package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleMeetSpaceDto {
    private final String spaceName;
    private final String meetingUrl;
    private final String meetingCode;
}
