package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String profession;

    private String bioShort;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String completeDescription;

    private int availabilityMask;

    private String limits;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String competences;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private Double reputationScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reputationSummary;

    @Column(nullable = false)
    private Integer feedbackCountAtLastEvaluation = 0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

