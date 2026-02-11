package com.example.skillswap.controller;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.ChatService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChatRoom(
            @RequestParam Long otherUserId,
            Principal principal) {
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
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(Principal principal) {
        try {
            List<ChatRoom> chatRooms = chatService.getUserChatRooms(principal);
            return ResponseEntity.ok(chatRooms);
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
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("email", principal.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to get current user ID from Principal
    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User user = chatService.getUserByEmail(principal.getName());
        return user.getId();
    }
}
