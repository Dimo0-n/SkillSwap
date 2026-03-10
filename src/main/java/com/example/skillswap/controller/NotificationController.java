package com.example.skillswap.controller;

import com.example.skillswap.dto.NotificationDTO;
import com.example.skillswap.service.impl.ChatService;
import com.example.skillswap.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "20") int limit,
            Principal principal) {
        Long userId = chatService.getCurrentUserId(principal);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId, limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        Long userId = chatService.getCurrentUserId(principal);
        long unreadCount = notificationService.getUnreadCountForUser(userId);

        Map<String, Long> body = new HashMap<>();
        body.put("unreadCount", unreadCount);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId, Principal principal) {
        Long userId = chatService.getCurrentUserId(principal);
        boolean updated = notificationService.markAsRead(userId, notificationId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", updated);
        body.put("unreadCount", notificationService.getUnreadCountForUser(userId));
        return updated ? ResponseEntity.ok(body) : ResponseEntity.notFound().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
        Long userId = chatService.getCurrentUserId(principal);
        int affected = notificationService.markAllAsRead(userId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("updated", affected);
        body.put("unreadCount", notificationService.getUnreadCountForUser(userId));
        return ResponseEntity.ok(body);
    }
}
