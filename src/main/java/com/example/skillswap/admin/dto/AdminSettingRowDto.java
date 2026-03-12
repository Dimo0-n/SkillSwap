package com.example.skillswap.admin.dto;

public record AdminSettingRowDto(
        String key,
        String label,
        String category,
        String value,
        String description
) {
}