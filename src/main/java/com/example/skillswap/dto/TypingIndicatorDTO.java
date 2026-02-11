package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {
    private Long chatRoomId;
    private Long userId;
    private String userName;
    private Boolean isTyping;
}
