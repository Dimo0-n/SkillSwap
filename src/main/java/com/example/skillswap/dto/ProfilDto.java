package com.example.skillswap.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Base64;
import java.util.List;

@Getter
@Setter
public class ProfilDto {

    private Long id;
    private String name;
    private String profession;
    private String bioShort;
    private List<String> competences;
    private String completeDescription;
    private int availabilityMask;
    private List<String> limits;
    private List<String> strengths;
    private String imageUrl;

}

