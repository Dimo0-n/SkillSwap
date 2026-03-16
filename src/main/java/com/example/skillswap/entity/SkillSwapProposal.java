package com.example.skillswap.entity;

import com.example.skillswap.enums.SkillSwapProposalStatus;
import com.example.skillswap.util.UtcDateTimes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_swap_proposal", indexes = {
        @Index(name = "idx_skill_swap_owner_status", columnList = "owner_id, status"),
        @Index(name = "idx_skill_swap_requester_status", columnList = "requester_id, status"),
        @Index(name = "idx_skill_swap_announce_requester", columnList = "announce_id, requester_id")
})
@Getter
@Setter
@NoArgsConstructor
public class SkillSwapProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "announce_id", nullable = false)
    private Announce announce;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 80)
    private String offeredSkill;

    @Column(nullable = false, length = 80)
    private String requestedSkill;

    @Column(length = 500)
    private String requesterMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SkillSwapProposalStatus status = SkillSwapProposalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime respondedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private Long cancelledByUserId;

    @Column(length = 255)
    private String cancellationReason;

    @PrePersist
    void onCreate() {
        LocalDateTime now = UtcDateTimes.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = SkillSwapProposalStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = UtcDateTimes.now();
    }
}
