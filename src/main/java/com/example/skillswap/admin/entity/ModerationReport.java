package com.example.skillswap.admin.entity;

import com.example.skillswap.admin.enums.ModerationReportStatus;
import com.example.skillswap.admin.enums.ReportTargetType;
import com.example.skillswap.entity.User;
import com.example.skillswap.util.UtcDateTimes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_report", indexes = {
        @Index(name = "idx_report_status_created", columnList = "status, createdAt"),
        @Index(name = "idx_report_target", columnList = "targetType, targetId"),
        @Index(name = "idx_report_reporter", columnList = "reporter_id, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class ModerationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 180)
    private String targetLabel;

    @Column(nullable = false, length = 200)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ModerationReportStatus status = ModerationReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @Column(length = 400)
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = UtcDateTimes.now();
        }
        if (status == null) {
            status = ModerationReportStatus.PENDING;
        }
    }
}