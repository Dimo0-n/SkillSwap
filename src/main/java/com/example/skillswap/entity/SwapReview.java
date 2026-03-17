package com.example.skillswap.entity;

import com.example.skillswap.util.UtcDateTimes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "swap_review", indexes = {
        @Index(name = "idx_swap_review_proposal_created", columnList = "proposal_id, created_at"),
        @Index(name = "idx_swap_review_reviewee_created", columnList = "reviewee_id, created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_swap_review_proposal_reviewer", columnNames = {"proposal_id", "reviewer_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class SwapReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private SkillSwapProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 300)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = UtcDateTimes.now();
        }
    }
}
