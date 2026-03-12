package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PresenceStatusDTO {
    private Long userId;
    private boolean online;
    private Instant lastSeenAt;
    private String timeZoneId;
}
