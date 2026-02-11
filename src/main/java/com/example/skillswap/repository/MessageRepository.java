package com.example.skillswap.repository;

import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.Message;
import com.example.skillswap.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    Page<Message> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoom.id = :chatRoomId AND m.sender.id != :userId AND m.status != 'SEEN'")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.id = :messageId")
    void updateMessageStatus(@Param("messageId") Long messageId, @Param("status") MessageStatus status);
}
