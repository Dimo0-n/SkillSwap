package com.example.skillswap.service;

import com.example.skillswap.dto.NotificationDTO;
import com.example.skillswap.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationDTO createNotification(Long recipientUserId,
                                       NotificationType type,
                                       String title,
                                       String message,
                                       String targetUrl);

    NotificationDTO createWelcomeNotification(Long recipientUserId);

    List<NotificationDTO> getNotificationsForUser(Long userId, int limit);

    long getUnreadCountForUser(Long userId);

    boolean markAsRead(Long userId, Long notificationId);

    int markAllAsRead(Long userId);
}
