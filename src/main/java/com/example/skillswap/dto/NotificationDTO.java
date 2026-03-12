package com.example.skillswap.dto;

import com.example.skillswap.entity.Notification;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.util.UtcDateTimes;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private Instant createdAt;
    private boolean read;
    private NotificationProposalDTO proposal;

    public static NotificationDTO basic(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setTargetUrl(notification.getTargetUrl());
        dto.setCreatedAt(UtcDateTimes.toInstant(notification.getCreatedAt()));
        dto.setRead(notification.getReadAt() != null);
        return dto;
    }
}
