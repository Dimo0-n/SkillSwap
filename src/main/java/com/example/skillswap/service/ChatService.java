package com.example.skillswap.service;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.ConversationSettingsDTO;
import com.example.skillswap.dto.ConversationSettingsUpdateRequest;
import com.example.skillswap.dto.ConversationSummaryDTO;
import com.example.skillswap.dto.MessageReactionDTO;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.User;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {

    ChatRoom createOrGetChatRoom(Long user1Id, Long user2Id);

    ChatMessageDTO sendMessage(ChatMessageDTO dto, Principal principal);

    ChatMessageDTO createSystemMessage(Long chatRoomId,
                                       Long senderUserId,
                                       String content,
                                       String systemTitle,
                                       String systemExchangeSummary,
                                       String systemStatusLabel);

    void markAsDelivered(Long messageId);

    void markAsSeen(Long messageId, Principal principal);

    MessageReactionDTO addReaction(MessageReactionDTO dto, Principal principal);

    MessageReactionDTO removeReaction(MessageReactionDTO dto, Principal principal);

    List<ChatMessageDTO> getChatHistory(Long chatRoomId, Principal principal, int page, int size);

    Long getUnreadCount(Long chatRoomId, Long userId);

    List<ChatRoom> getUserChatRooms(Principal principal);

    List<ConversationSummaryDTO> getConversationSummaries(Principal principal);

    ConversationSettingsDTO updateConversationSettings(Long chatRoomId,
                                                       ConversationSettingsUpdateRequest request,
                                                       Principal principal);

    boolean setUserOnline(Long userId);

    LocalDateTime setUserOffline(Long userId);

    List<Long> findInactiveOnlineUserIds(LocalDateTime cutoff);

    User getUserById(Long userId);

    Long getCurrentUserId(Principal principal);
}
