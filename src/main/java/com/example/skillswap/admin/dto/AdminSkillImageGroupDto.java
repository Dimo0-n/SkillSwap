package com.example.skillswap.admin.dto;

import java.util.List;

public record AdminSkillImageGroupDto(
        String skillName,
        List<String> imageUrls
) {
}