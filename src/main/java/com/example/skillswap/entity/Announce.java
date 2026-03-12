package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "announces")
public class Announce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;

    private String categoryOffered;

    private String categoryRequired;

    private String imageKey;

    private String imagePath;

    private String additionalInfo;

    private LocalDateTime date;

    @Column(nullable = false)
    private boolean markedAsSpam = false;

    @Column(nullable = false)
    private boolean deletedByAdmin = false;

    private LocalDateTime moderatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

