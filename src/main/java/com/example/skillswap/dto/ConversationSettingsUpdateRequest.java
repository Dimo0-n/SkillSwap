package com.example.skillswap.dto;

import lombok.Data;

@Data
public class ConversationSettingsUpdateRequest {
    private Boolean muted;
    private Boolean blocked;
    private Boolean reported;
}