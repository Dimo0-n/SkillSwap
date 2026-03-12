package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationSettingsDTO {
    private Long chatRoomId;
    private boolean muted;
    private boolean blocked;
    private boolean reported;
}