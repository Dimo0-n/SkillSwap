package com.example.skillswap.dto;

import com.example.skillswap.enums.Availability;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
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

    public List<String> getAvailabilityList() {
        if (availabilityMask == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(Availability.values())
                .filter(av -> (availabilityMask & av.getBit()) == av.getBit())
                .map(this::toReadableAvailability)
                .toList();
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

    private String toReadableAvailability(Availability availability) {
        return switch (availability) {
            case DIMINEATA -> "Dimineata";
            case DUPA_AMIAZA -> "Dupa-amiaza";
            case SEARA -> "Seara";
            case WEEKEND -> "Weekend";
        };
    }

}

