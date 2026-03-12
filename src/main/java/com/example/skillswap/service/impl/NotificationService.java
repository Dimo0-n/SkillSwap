package com.example.skillswap.service.impl;

import com.example.skillswap.dto.NotificationDTO;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.repository.NotificationRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements com.example.skillswap.service.NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO createNotification(Long recipientUserId,
                                              NotificationType type,
                                              String title,
                                              String message,
                                              String targetUrl) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);

        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = NotificationDTO.fromEntity(saved);

        // Private real-time event for the recipient.
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", dto);
        return dto;
    }

    @Transactional
    public NotificationDTO createWelcomeNotification(Long recipientUserId) {
        return createNotification(
                recipientUserId,
                NotificationType.WELCOME,
                "Bine ai venit pe SkillSwap",
                "Iti multumim ca te-ai alaturat comunitatii SkillSwap.",
                "/profile/complete"
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(Long userId, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, boundedLimit))
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCountForUser(Long userId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElse(null);
        if (notification == null) {
            return false;
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(UtcDateTimes.now());
        }

        return true;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadAtIsNull(userId);
        if (unread.isEmpty()) {
            return 0;
        }

        LocalDateTime now = UtcDateTimes.now();
        unread.forEach(item -> item.setReadAt(now));
        return unread.size();
    }
}
