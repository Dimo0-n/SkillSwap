package com.example.skillswap.dto;

import com.example.skillswap.enums.Availability;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
public class ProfilDto {

    private Long id;
    private Long userId;
    @NotBlank(message = "{profile.validation.name.required}")
    private String name;
    @NotBlank(message = "{profile.validation.profession.required}")
    private String profession;
    @NotBlank(message = "{profile.validation.bio.required}")
    private String bioShort;
    private String competences;          // comma separated
    @NotBlank(message = "{profile.validation.description.required}")
    private String completeDescription;
    private int availabilityMask;
    private String limits;               // comma separated
    private String strengths;            // comma separated
    private String imageUrl;
    private Double reputationScore;
    private String reputationSummary;
    private Integer feedbackCountAtLastEvaluation;

    public List<String> getCompetenceList() {
        return splitCommaSeparated(competences);
    }

    public List<String> getLimitsList() {
        return splitCommaSeparated(limits);
    }

    public List<String> getStrengthsList() {
        return splitCommaSeparated(strengths);
    }

    public List<String> getStrengthsDisplayList() {
        return getStrengthsList().stream()
                .map(this::toReadableStrength)
                .toList();
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

    public int getReputationCircleValue() {
        if (reputationScore == null) {
            return 0;
        }

        return Math.max(0, Math.min(100, (int) Math.round(reputationScore * 10)));
    }

    public String getReputationScoreDisplay() {
        if (reputationScore == null) {
            return "E";
        }

        return String.format(Locale.US, "%.1f", reputationScore);
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

    private String toReadableStrength(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return switch (value) {
            case "clear-communication" -> "Comunicare clara";
            case "simple-explanations" -> "Explic pe intelesul tuturor";
            case "reliable-schedule" -> "Serios cu programarile";
            case "easy-collaboration" -> "Gasesc limba comuna cu fiecare";
            case "patient" -> "Rabdator si intelegator";
            case "organized" -> "Organizat si structurat";
            default -> capitalizeWords(value.replace("-", " "));
        };
    }

    private String capitalizeWords(String text) {
        String[] parts = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "ProfilDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", profession='" + profession + '\'' +
                ", bioShort='" + bioShort + '\'' +
                ", competences='" + competences + '\'' +
                ", completeDescription='" + completeDescription + '\'' +
                ", availabilityMask=" + availabilityMask +
                ", limits='" + limits + '\'' +
                ", strengths='" + strengths + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}

