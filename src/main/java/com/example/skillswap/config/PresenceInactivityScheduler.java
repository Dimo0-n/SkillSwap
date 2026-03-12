package com.example.skillswap.config;

import com.example.skillswap.dto.PresenceStatusDTO;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PresenceInactivityScheduler {

    private static final int OFFLINE_TIMEOUT_SECONDS = 45;

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 15000)
    public void markInactiveUsersOffline() {
        var cutoff = UtcDateTimes.now().minusSeconds(OFFLINE_TIMEOUT_SECONDS);

        for (Long userId : chatService.findInactiveOnlineUserIds(cutoff)) {
            try {
                var lastSeenAt = chatService.setUserOffline(userId);
                var user = chatService.getUserById(userId);
                messagingTemplate.convertAndSend(
                        "/topic/presence",
                        new PresenceStatusDTO(userId, false, UtcDateTimes.toInstant(lastSeenAt), user.getTimeZoneId())
                );
            } catch (Exception ex) {
                System.err.println("[PRESENCE] Failed to set inactive user offline: " + ex.getMessage());
            }
        }
    }
}
