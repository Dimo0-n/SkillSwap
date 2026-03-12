package com.example.skillswap.entity;

import com.example.skillswap.util.UtcDateTimes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participant_settings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conversation_participant_settings_room_user", columnNames = { "chat_room_id", "user_id" })
}, indexes = {
        @Index(name = "idx_conversation_participant_settings_user", columnList = "user_id"),
        @Index(name = "idx_conversation_participant_settings_room", columnList = "chat_room_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ConversationParticipantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean muted;

    @Column(nullable = false)
    private boolean blocked;

    @Column(nullable = false)
    private boolean reported;

    private LocalDateTime reportedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = UtcDateTimes.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = UtcDateTimes.now();
    }
}