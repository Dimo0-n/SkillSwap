package com.example.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SwapReviewDto {
    private Long id;
    private Long proposalId;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
