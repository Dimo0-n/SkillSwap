package com.example.skillswap.admin.dto;

public record AdminSkillRowDto(
        String canonicalName,
        long occurrences,
        String variants
) {
}