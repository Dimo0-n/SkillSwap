package com.example.skillswap.dto;

import com.example.skillswap.entity.Notification;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.util.UtcDateTimes;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private Instant createdAt;
    private boolean read;

    public static NotificationDTO fromEntity(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetUrl(),
                UtcDateTimes.toInstant(notification.getCreatedAt()),
                notification.getReadAt() != null
        );
    }
}
