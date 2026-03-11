package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ChatMessageDTO;
import com.example.skillswap.dto.ConversationSummaryDTO;
import com.example.skillswap.dto.MessageReactionDTO;
import com.example.skillswap.entity.*;
import com.example.skillswap.enums.MessageStatus;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.repository.ChatRoomRepository;
import com.example.skillswap.repository.MessageReactionRepository;
import com.example.skillswap.repository.MessageRepository;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final ProfileCompletionService profileCompletionService;

    @Transactional
    public ChatRoom createOrGetChatRoom(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        return chatRoomRepository.findByUsers(user1, user2)
                .orElseGet(() -> {
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUser1(user1);
                    chatRoom.setUser2(user2);
                    return chatRoomRepository.save(chatRoom);
                });
    }

    @Transactional
    public ChatMessageDTO sendMessage(ChatMessageDTO dto, Principal principal) {
        Long currentUserId = extractCurrentUserId(principal);
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (!profileCompletionService.isProfileCompleted(sender)) {
            throw new RuntimeException("Profile completion is required before sending messages");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Security check
        if (!chatRoom.getUser1().getId().equals(sender.getId()) &&
                !chatRoom.getUser2().getId().equals(sender.getId())) {
            throw new RuntimeException("Unauthorized access to chat room");
        }

        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setStatus(MessageStatus.SENT);

        Message saved = messageRepository.save(message);

        User recipient = chatRoom.getUser1().getId().equals(sender.getId())
                ? chatRoom.getUser2()
                : chatRoom.getUser1();

        String messagePreview = saved.getContent() == null ? "" : saved.getContent().trim();
        if (messagePreview.length() > 120) {
            messagePreview = messagePreview.substring(0, 117) + "...";
        }

        notificationService.createNotification(
                recipient.getId(),
                NotificationType.NEW_MESSAGE,
                "Mesaj nou",
                sender.getFullName() + ": " + messagePreview,
                "/chat-history?roomId=" + chatRoom.getId()
        );

        return convertToDTO(saved, sender.getId());
    }

    @Transactional
    public void markAsDelivered(Long messageId) {
        messageRepository.updateMessageStatus(messageId, MessageStatus.DELIVERED);
    }

    @Transactional
    public void markAsSeen(Long messageId, Principal principal) {
                Long currentUserId = extractCurrentUserId(principal);
                User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only mark as seen if the user is the recipient
        if (!message.getSender().getId().equals(user.getId())) {
            messageRepository.updateMessageStatus(messageId, MessageStatus.SEEN);
        }
    }

    @Transactional
    public MessageReactionDTO addReaction(MessageReactionDTO dto, Principal principal) {
                Long currentUserId = extractCurrentUserId(principal);
                User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageRepository.findById(dto.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Check if user already reacted
        MessageReaction reaction = reactionRepository.findByMessageAndUser(message, user)
                .orElse(new MessageReaction());

        reaction.setMessage(message);
        reaction.setUser(user);
        reaction.setEmoji(dto.getEmoji());

        MessageReaction saved = reactionRepository.save(reaction);

        dto.setUserId(user.getId());
        dto.setUserName(user.getFullName());
        dto.setRemoved(false);
        return dto;
    }

    @Transactional
    public MessageReactionDTO removeReaction(MessageReactionDTO dto, Principal principal) {
                Long currentUserId = extractCurrentUserId(principal);
                User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageRepository.findById(dto.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        MessageReaction reaction = reactionRepository.findByMessageAndUser(message, user)
                .orElseThrow(() -> new RuntimeException("Reaction not found"));

        dto.setEmoji(reaction.getEmoji());
        reactionRepository.delete(reaction);

        dto.setUserId(user.getId());
        dto.setUserName(user.getFullName());
        dto.setRemoved(true);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Long chatRoomId, Principal principal, int page, int size) {
                Long currentUserId = extractCurrentUserId(principal);
                User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Security check
        if (!chatRoom.getUser1().getId().equals(user.getId()) &&
                !chatRoom.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to chat room");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable);

        return messages.getContent().stream()
                .map(msg -> convertToDTO(msg, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long chatRoomId, Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, userId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getUserChatRooms(Principal principal) {
                Long currentUserId = extractCurrentUserId(principal);
                User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatRoomRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryDTO> getConversationSummaries(Principal principal) {
                Long currentUserId = extractCurrentUserId(principal);
                User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUser(currentUser);

        return chatRooms.stream()
                .map(chatRoom -> {
                    User otherUser = chatRoom.getUser1().getId().equals(currentUser.getId())
                            ? chatRoom.getUser2()
                            : chatRoom.getUser1();

                    Message lastMessage = messageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    Long unreadCount = messageRepository.countUnreadMessages(chatRoom.getId(), currentUser.getId());
                    String avatarUrl = profileRepository.findFirstByUserEmailOrderByIdDesc(otherUser.getEmail())
                            .map(p -> "/profile/image/" + otherUser.getEmail())
                            .orElse("/img/default-avatar.png");

                    return new ConversationSummaryDTO(
                            chatRoom.getId(),
                            otherUser.getId(),
                            otherUser.getFullName(),
                            avatarUrl,
                            lastMessage != null ? lastMessage.getContent() : "",
                            lastMessage != null ? lastMessage.getCreatedAt() : chatRoom.getCreatedAt(),
                            unreadCount,
                            Boolean.TRUE.equals(otherUser.getOnline()),
                            otherUser.getLastSeenAt()
                    );
                })
                .sorted(Comparator.comparing(ConversationSummaryDTO::getLastMessageTime).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
        public boolean setUserOnline(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

                boolean alreadyOnline = Boolean.TRUE.equals(user.getOnline());
        user.setOnline(true);
                user.setLastActivityAt(LocalDateTime.now());
        user.setLastSeenAt(null);
                return !alreadyOnline;
    }

    @Transactional
    public LocalDateTime setUserOffline(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        user.setOnline(false);
                user.setLastActivityAt(null);
        user.setLastSeenAt(now);
        return now;
    }

        @Transactional(readOnly = true)
        public List<Long> findInactiveOnlineUserIds(LocalDateTime cutoff) {
                return userRepository.findByOnlineTrueAndLastActivityAtBefore(cutoff)
                                .stream()
                                .map(User::getId)
                                .toList();
        }

        @Transactional(readOnly = true)
        public User getUserById(Long userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
        }

        @Transactional(readOnly = true)
        public Long getCurrentUserId(Principal principal) {
                return extractCurrentUserId(principal);
        }

        private Long extractCurrentUserId(Principal principal) {
                if (!(principal instanceof Authentication authentication)) {
                        throw new RuntimeException("User not authenticated");
                }

                Object authPrincipal = authentication.getPrincipal();

                if (authPrincipal instanceof CustomUserDetails customUserDetails) {
                        return customUserDetails.getId();
                }

                if (authPrincipal instanceof OAuth2User oauth2User) {
                        Object userId = oauth2User.getAttribute("userId");
                        if (userId instanceof Number number) {
                                return number.longValue();
                        }

                        String email = oauth2User.getAttribute("email");
                        if (email != null && !email.isBlank()) {
                                return userRepository.findByEmail(email)
                                                .map(User::getId)
                                                .orElseThrow(() -> new RuntimeException("User not found"));
                        }
                }

                throw new RuntimeException("Unable to resolve current user id");
        }

    private ChatMessageDTO convertToDTO(Message message, Long currentUserId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setTimestamp(message.getCreatedAt());

        // Get reaction summary
        List<Object[]> reactionCounts = reactionRepository.countReactionsByEmoji(message);
        List<ChatMessageDTO.ReactionSummary> reactions = new ArrayList<>();

        for (Object[] row : reactionCounts) {
            String emoji = (String) row[0];
            Long count = (Long) row[1];

            // Check if current user reacted with this emoji
            boolean currentUserReacted = message.getReactions().stream()
                    .anyMatch(r -> r.getEmoji().equals(emoji) && r.getUser().getId().equals(currentUserId));

            reactions.add(new ChatMessageDTO.ReactionSummary(emoji, count, currentUserReacted));
        }

        dto.setReactions(reactions);

        return dto;
    }
}
