package com.example.skillswap.admin.dto;

import java.util.List;

public record AdminStatisticsDto(
        List<AdminSummaryMetricDto> metrics,
        List<AdminChartPointDto> userGrowth,
        List<AdminChartPointDto> activeSessions,
        List<AdminChartPointDto> completedSessions,
        List<AdminChartPointDto> popularSkills
) {
}