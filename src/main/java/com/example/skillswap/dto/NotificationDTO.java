package com.example.skillswap.dto;

import com.example.skillswap.entity.Notification;
import com.example.skillswap.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private LocalDateTime createdAt;
    private boolean read;

    public static NotificationDTO fromEntity(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetUrl(),
                notification.getCreatedAt(),
                notification.getReadAt() != null
        );
    }
}
