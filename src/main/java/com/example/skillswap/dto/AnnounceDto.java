package com.example.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnounceDto {

    private Long id;

    @NotBlank(message = "{announce.validation.title.required}")
    @Size(min = 3, max = 100, message = "{announce.validation.title.size}")
    private String title;

    @NotBlank(message = "{announce.validation.description.required}")
    @Size(min = 10, max = 2000, message = "{announce.validation.description.size}")
    private String description;

    @NotBlank(message = "{announce.validation.author.required}")
    @Size(min = 2, max = 80, message = "{announce.validation.author.size}")
    private String author;

    @NotBlank(message = "{announce.validation.category.offered.required}")
    @Size(min = 2, max = 60, message = "{announce.validation.category.offered.size}")
    private String categoryOffered;

    @NotBlank(message = "{announce.validation.category.required.required}")
    @Size(min = 2, max = 60, message = "{announce.validation.category.required.size}")
    private String categoryRequired;

    private String imageKey;

    private String imagePath;

    @Size(max = 160, message = "{announce.validation.additionalInfo.size}")
    private String additionalInfo;

    private LocalDateTime date;

    private Long userId;
}
