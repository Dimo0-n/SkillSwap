package com.example.skillswap.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
public class ProfilDto {

    private Long id;
    private String name;
    private String profession;
    private String bioShort;
    private String competences;          // comma separated
    private String completeDescription;
    private int availabilityMask;
    private String limits;               // comma separated
    private String strengths;            // comma separated
    private String imageUrl;

    public List<String> getCompetenceList() {
        return splitCommaSeparated(competences);
    }

    public List<String> getLimitsList() {
        return splitCommaSeparated(limits);
    }

    public List<String> getStrengthsList() {
        return splitCommaSeparated(strengths);
    }

    private List<String> splitCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

}

