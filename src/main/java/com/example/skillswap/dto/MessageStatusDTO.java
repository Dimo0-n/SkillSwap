package com.example.skillswap.dto;

import com.example.skillswap.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusDTO {
    private Long messageId;
    private MessageStatus status;
}
