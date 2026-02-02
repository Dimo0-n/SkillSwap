package com.example.skillswap.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnounceDto {

    private Long id;

    private String title;

    private String description;

    private String author;

    private String categoryOffered;

    private String categoryRequired;

    private String imageKey;

    private String imagePath;

    private String additionalInfo;

    private LocalDateTime date;

    private int userId;

}
