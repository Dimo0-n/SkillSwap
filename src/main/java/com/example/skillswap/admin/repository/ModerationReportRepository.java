package com.example.skillswap.admin.repository;

import com.example.skillswap.admin.entity.ModerationReport;
import com.example.skillswap.admin.enums.ModerationReportStatus;
import com.example.skillswap.admin.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, Long> {

    long countByStatus(ModerationReportStatus status);

    boolean existsByTargetTypeAndTargetIdAndReporterId(ReportTargetType targetType, Long targetId, Long reporterId);

    List<ModerationReport> findTop10ByStatusOrderByCreatedAtDesc(ModerationReportStatus status);
}