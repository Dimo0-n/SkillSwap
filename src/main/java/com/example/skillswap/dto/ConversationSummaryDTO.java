package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationSummaryDTO {
    private Long chatRoomId;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
