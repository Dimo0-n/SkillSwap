package com.example.skillswap.dto;

import com.example.skillswap.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageStatus status;
    private LocalDateTime timestamp;
    private List<ReactionSummary> reactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionSummary {
        private String emoji;
        private Long count;
        private Boolean currentUserReacted;
    }
}
