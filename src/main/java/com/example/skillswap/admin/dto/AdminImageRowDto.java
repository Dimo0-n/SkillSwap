package com.example.skillswap.admin.dto;

public record AdminImageRowDto(
        String imageType,
        Long entityId,
        String ownerName,
        String sourceLabel,
        String previewUrl,
        String publicId,
        boolean suspicious
) {
}