package com.example.skillswap.controller;

import com.example.skillswap.dto.PresenceStatusDTO;
import com.example.skillswap.dto.UserTimeZoneUpdateRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.UserTimeZoneService;
import com.example.skillswap.util.UtcDateTimes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserTimeZoneController {

    private final ChatService chatService;
    private final UserTimeZoneService userTimeZoneService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/timezone")
    public ResponseEntity<Void> updateCurrentUserTimeZone(@Valid @RequestBody UserTimeZoneUpdateRequest request,
                                                          Principal principal) {
        Long userId = chatService.getCurrentUserId(principal);
        UserTimeZoneService.TimeZoneUpdateResult updateResult =
                userTimeZoneService.updateTimeZone(userId, request.getTimeZoneId());

        if (updateResult.changed()) {
            User user = updateResult.user();
            messagingTemplate.convertAndSend(
                    "/topic/presence",
                    new PresenceStatusDTO(
                            user.getId(),
                            Boolean.TRUE.equals(user.getOnline()),
                            UtcDateTimes.toInstant(user.getLastSeenAt()),
                            user.getTimeZoneId()
                    )
            );
        }

        return ResponseEntity.noContent().build();
    }
}
