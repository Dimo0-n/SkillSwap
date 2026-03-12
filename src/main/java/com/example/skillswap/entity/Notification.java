package com.example.skillswap.entity;

import com.example.skillswap.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import com.example.skillswap.util.UtcDateTimes;

@Entity
@Table(name = "app_notification", indexes = {
        @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, createdAt"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, readAt")
})
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 255)
    private String targetUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_swap_proposal_id")
    private SkillSwapProposal skillSwapProposal;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = UtcDateTimes.now();
        }
    }
}
