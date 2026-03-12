package com.example.skillswap.admin.dto;

import java.util.List;

public record AdminDashboardOverviewDto(
        List<AdminSummaryMetricDto> metrics,
        List<AdminChartPointDto> userRegistrations,
        List<AdminChartPointDto> sessionsByWeek,
        List<AdminChartPointDto> popularSkills,
        List<AdminRecentActionDto> recentActions,
        List<AdminReportRowDto> pendingReports
) {
}