package com.example.skillswap.controller;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.ConversationSettingsDTO;
import com.example.skillswap.dto.ConversationSettingsUpdateRequest;
import com.example.skillswap.dto.ConversationSummaryDTO;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.ProfileCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ProfileCompletionService profileCompletionService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChatRoom(
            @RequestParam Long otherUserId,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildAuthenticationRequiredResponse());
        }

        if (!profileCompletionService.isProfileCompleted(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildProfileCompletionRequiredResponse());
        }

        try {
            // Get current user ID from UserService or repository
            // For now, we'll need to get it from the email
            ChatRoom chatRoom = chatService.createOrGetChatRoom(
                    getCurrentUserId(principal),
                    otherUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("chatRoomId", chatRoom.getId());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Principal principal) {
        try {
            List<ChatMessageDTO> messages = chatService.getChatHistory(chatRoomId, principal, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ConversationSummaryDTO>> getUserChatRooms(Principal principal) {
        try {
            List<ConversationSummaryDTO> conversations = chatService.getConversationSummaries(principal);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/rooms/{chatRoomId}/settings")
    public ResponseEntity<ConversationSettingsDTO> updateConversationSettings(
            @PathVariable Long chatRoomId,
            @RequestBody ConversationSettingsUpdateRequest request,
            Principal principal) {
        try {
            return ResponseEntity.ok(chatService.updateConversationSettings(chatRoomId, request, principal));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread/{chatRoomId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long chatRoomId,
            Principal principal) {
        try {
            Long count = chatService.getUnreadCount(chatRoomId, getCurrentUserId(principal));
            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            var user = chatService.getUserById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("email", principal.getName());
            response.put("timeZoneId", user.getTimeZoneId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to get current user ID from Principal
    private Long getCurrentUserId(Principal principal) {
        return chatService.getCurrentUserId(principal);
    }

    private Map<String, Object> buildProfileCompletionRequiredResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Complete your profile before using this feature.");
        body.put("redirectUrl", ProfileCompletionService.REQUIRED_REDIRECT_PATH);
        return body;
    }

    private Map<String, Object> buildAuthenticationRequiredResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Authentication is required before using this feature.");
        body.put("redirectUrl", "/login");
        return body;
    }
}
