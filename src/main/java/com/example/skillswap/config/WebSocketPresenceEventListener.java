package com.example.skillswap.config;

import com.example.skillswap.dto.PresenceStatusDTO;
import com.example.skillswap.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> userSessionCount = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) {
            return;
        }

        try {
            Long userId = chatService.getCurrentUserId(principal);
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                sessionUserMap.put(sessionId, userId);
            }

            AtomicInteger counter = userSessionCount.computeIfAbsent(userId, id -> new AtomicInteger(0));
            int activeSessions = counter.incrementAndGet();

            if (activeSessions == 1) {
                boolean becameOnline = chatService.setUserOnline(userId);
                if (becameOnline) {
                    messagingTemplate.convertAndSend("/topic/presence", new PresenceStatusDTO(userId, true, null));
                }
            }
        } catch (Exception ex) {
            System.err.println("[PRESENCE] Failed to mark user online: " + ex.getMessage());
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long userId = null;
        if (sessionId != null) {
            userId = sessionUserMap.remove(sessionId);
        }

        if (userId == null && accessor.getUser() != null) {
            try {
                userId = chatService.getCurrentUserId(accessor.getUser());
            } catch (Exception ignored) {
                return;
            }
        }

        if (userId == null) {
            return;
        }

        AtomicInteger counter = userSessionCount.get(userId);
        if (counter == null) {
            return;
        }

        int activeSessions = counter.decrementAndGet();
        if (activeSessions <= 0) {
            userSessionCount.remove(userId);

            try {
                LocalDateTime lastSeenAt = chatService.setUserOffline(userId);
                messagingTemplate.convertAndSend("/topic/presence", new PresenceStatusDTO(userId, false, lastSeenAt));
            } catch (Exception ex) {
                System.err.println("[PRESENCE] Failed to mark user offline: " + ex.getMessage());
            }
        }
    }
}
