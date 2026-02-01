package com.example.skillswap.dto;

import com.example.skillswap.entity.AnnounceImage;
import com.example.skillswap.entity.User;
import jakarta.persistence.*;
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

    private String additionalInfo;

    private LocalDateTime date;

    private int userId;

}
