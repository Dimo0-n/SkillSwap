package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "video_room", indexes = {
        @Index(name = "idx_video_room_chat_room_active", columnList = "chat_room_id, active"),
        @Index(name = "idx_video_room_space_name", columnList = "space_name")
})
public class VideoRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "space_name", nullable = false, length = 128)
    private String spaceName;

    @Column(name = "meeting_url", nullable = false, length = 512)
    private String meetingUrl;

    @Column(name = "meeting_code", nullable = false, length = 64)
    private String meetingCode;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant lastValidatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastValidatedAt == null) {
            lastValidatedAt = now;
        }
    }
}
