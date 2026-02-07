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

    @NotBlank(message = "Titlul este obligatoriu")
    @Size(min = 3, max = 100, message = "Titlul trebuie să aibă între 3 și 100 de caractere")
    private String title;

    @NotBlank(message = "Descrierea este obligatorie")
    @Size(min = 10, message = "Descrierea trebuie sa fie mai detaliata")
    private String description;

    private String author;

    private String categoryOffered;

    private String categoryRequired;

    private String imageKey;

    private String imagePath;

    private String additionalInfo;

    private LocalDateTime date;

    private Long userId;

}
