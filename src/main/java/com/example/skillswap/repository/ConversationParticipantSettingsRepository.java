package com.example.skillswap.repository;

import com.example.skillswap.entity.ConversationParticipantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantSettingsRepository extends JpaRepository<ConversationParticipantSettings, Long> {

    Optional<ConversationParticipantSettings> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ConversationParticipantSettings> findByUserIdAndChatRoomIdIn(Long userId, Collection<Long> chatRoomIds);

    boolean existsByChatRoomIdAndUserIdAndMutedTrue(Long chatRoomId, Long userId);

    boolean existsByChatRoomIdAndBlockedTrue(Long chatRoomId);
}