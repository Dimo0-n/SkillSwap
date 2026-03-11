package com.example.skillswap.controller;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.MessageReactionDTO;
import com.example.skillswap.dto.MessageStatusDTO;
import com.example.skillswap.dto.TypingIndicatorDTO;
import com.example.skillswap.enums.MessageStatus;
import com.example.skillswap.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO message, Principal principal) {
        try {
            // Save message and get DTO with ID
            ChatMessageDTO savedMessage = chatService.sendMessage(message, principal);

            // Broadcast to chat room
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + savedMessage.getChatRoomId(),
                    savedMessage);
        } catch (Exception e) {
            // Log error
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.delivered")
    public void markAsDelivered(@Payload MessageStatusDTO statusDTO, Principal principal) {
        try {
            chatService.markAsDelivered(statusDTO.getMessageId());

            // Notify sender about delivery
            messagingTemplate.convertAndSend(
                    "/topic/chat/status/" + statusDTO.getMessageId(),
                    new MessageStatusDTO(statusDTO.getMessageId(), MessageStatus.DELIVERED));
        } catch (Exception e) {
            System.err.println("Error marking as delivered: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.seen")
    public void markAsSeen(@Payload MessageStatusDTO statusDTO, Principal principal) {
        try {
            chatService.markAsSeen(statusDTO.getMessageId(), principal);

            // Notify sender about seen status
            messagingTemplate.convertAndSend(
                    "/topic/chat/status/" + statusDTO.getMessageId(),
                    new MessageStatusDTO(statusDTO.getMessageId(), MessageStatus.SEEN));
        } catch (Exception e) {
            System.err.println("Error marking as seen: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.react")
    public void addReaction(@Payload MessageReactionDTO reactionDTO, Principal principal) {
        try {
            MessageReactionDTO savedReaction = chatService.addReaction(reactionDTO, principal);

            // Broadcast reaction to all users in the chat
            messagingTemplate.convertAndSend(
                    "/topic/chat/reactions/" + reactionDTO.getMessageId(),
                    savedReaction);
        } catch (Exception e) {
            System.err.println("Error adding reaction: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.unreact")
    public void removeReaction(@Payload MessageReactionDTO reactionDTO, Principal principal) {
        try {
            MessageReactionDTO removedReaction = chatService.removeReaction(reactionDTO, principal);

            messagingTemplate.convertAndSend(
                    "/topic/chat/reactions/" + reactionDTO.getMessageId(),
                    removedReaction);
        } catch (Exception e) {
            System.err.println("Error removing reaction: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(@Payload TypingIndicatorDTO typingDTO, Principal principal) {
        try {
            // Populate user info from principal
            if (principal != null) {
                Long currentUserId = chatService.getCurrentUserId(principal);
                var user = chatService.getUserById(currentUserId);
                typingDTO.setUserId(user.getId());
                typingDTO.setUserName(user.getFullName());
            }
            
            // No persistence needed, just broadcast
            messagingTemplate.convertAndSend(
                    "/topic/chat/typing/" + typingDTO.getChatRoomId(),
                    typingDTO);
        } catch (Exception e) {
            System.err.println("Error sending typing indicator: " + e.getMessage());
        }
    }
}
