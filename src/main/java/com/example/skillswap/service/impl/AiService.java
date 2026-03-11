package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ProfileReputationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiService implements com.example.skillswap.service.AiService {

    private static final Pattern SCORE_PATTERN = Pattern.compile("\"?score\"?\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("\"?summary\"?\\s*[:=]\\s*\"?(.+?)\"?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(@Value("${deepseek.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String generateDescription(String ideas) {
        if (ideas == null || ideas.trim().isEmpty()) {
            throw new IllegalArgumentException("Scrie cateva idei inainte de generare.");
        }

        return requestChatCompletion(
                "You are an assistant that writes clear and honest profile descriptions for a skill sharing platform.",
                createDescriptionPrompt(ideas.trim()),
                0.7
        );
    }

    @Override
    public ProfileReputationResult evaluateProfileReputation(List<String> feedbackComments) {
        if (feedbackComments == null || feedbackComments.size() < 4) {
            throw new IllegalArgumentException("Sunt necesare cel putin 4 feedback-uri pentru evaluare.");
        }

        String response = requestChatCompletion(
                """
                You analyze reputation for a skill-sharing platform.
                Return only valid JSON with this exact shape:
                {"score":8.5,"summary":"..."}
                Rules:
                - score must be a number between 0 and 10
                - summary must be neutral, concise, and in Romanian
                - summary must be 2 to 3 sentences
                - do not include markdown
                - do not include extra keys
                """,
                createReputationPrompt(feedbackComments),
                0.3
        );

        return parseProfileReputation(response);
    }

    private String createDescriptionPrompt(String ideas) {
        return """
        Transform the following short ideas into a profile description
        for a skill-sharing platform called SkillSwap.

        Rules:
        - the description must be in the original language
        - 3 to 5 sentences
        - clear and natural language
        - describe what the user offers
        - describe how they work
        - mention who they are a good fit for
        - mention who they are NOT a good fit for
        - avoid marketing language
        - keep it realistic and honest

        User ideas:
        %s
        """.formatted(ideas);
    }

    private String createReputationPrompt(List<String> feedbackComments) {
        String joinedComments = feedbackComments.stream()
                .map(String::trim)
                .filter(comment -> !comment.isEmpty())
                .map(comment -> "- " + comment)
                .reduce((left, right) -> left + "\n" + right)
                .orElseThrow(() -> new IllegalArgumentException("Lista de feedback-uri este goala."));

        return """
        Analyze the following feedback comments written by users about a person on a skill exchange platform.
        Based on these comments, produce:
        1. A reputation score between 0 and 10
        2. A short neutral summary (2-3 sentences) describing the person's strengths and collaboration style.

        Comments:
        %s
        """.formatted(joinedComments);
    }

    private String requestChatCompletion(String systemPrompt, String userPrompt, double temperature) {
        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "temperature", temperature,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    JsonNode contentNode = node.path("choices").path(0).path("message").path("content");
                    String generated = contentNode.asText("").trim();

                    if (generated.isBlank()) {
                        throw new IllegalStateException("AI response was empty");
                    }

                    return generated;
                })
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("AI service did not return a response"));
    }

    private ProfileReputationResult parseProfileReputation(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return toProfileReputationResult(root.path("score").asDouble(Double.NaN), root.path("summary").asText(""));
        } catch (JsonProcessingException ignored) {
            Matcher scoreMatcher = SCORE_PATTERN.matcher(rawResponse);
            Matcher summaryMatcher = SUMMARY_PATTERN.matcher(rawResponse);

            if (!scoreMatcher.find() || !summaryMatcher.find()) {
                throw new IllegalStateException("AI reputation response could not be parsed");
            }

            double score = Double.parseDouble(scoreMatcher.group(1));
            String summary = summaryMatcher.group(1).trim().replaceAll("^\"|\"$", "");
            return toProfileReputationResult(score, summary);
        }
    }

    private ProfileReputationResult toProfileReputationResult(double score, String summary) {
        if (Double.isNaN(score)) {
            throw new IllegalStateException("AI reputation score is missing");
        }

        String normalizedSummary = summary == null ? "" : summary.trim();
        if (normalizedSummary.isBlank()) {
            throw new IllegalStateException("AI reputation summary is missing");
        }

        double normalizedScore = Math.max(0.0, Math.min(10.0, score));
        return new ProfileReputationResult(normalizedScore, normalizedSummary);
    }
}
