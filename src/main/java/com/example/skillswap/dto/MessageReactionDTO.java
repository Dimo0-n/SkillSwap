package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionDTO {
    private Long messageId;
    private Long userId;
    private String userName;
    private String emoji;
}
