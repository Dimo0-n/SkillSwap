package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PresenceStatusDTO {
    private Long userId;
    private boolean online;
    private LocalDateTime lastSeenAt;
}
