package com.example.skillswap.admin.dto;

public record AdminSummaryMetricDto(
        String label,
        String value,
        String context,
        String tone
) {
}