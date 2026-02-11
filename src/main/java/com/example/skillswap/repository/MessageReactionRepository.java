package com.example.skillswap.repository;

import com.example.skillswap.entity.Message;
import com.example.skillswap.entity.MessageReaction;
import com.example.skillswap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    List<MessageReaction> findByMessage(Message message);

    Optional<MessageReaction> findByMessageAndUser(Message message, User user);

    @Query("SELECT mr.emoji, COUNT(mr) FROM MessageReaction mr WHERE mr.message = :message GROUP BY mr.emoji")
    List<Object[]> countReactionsByEmoji(@Param("message") Message message);
}
