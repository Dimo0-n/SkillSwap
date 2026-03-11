package com.example.skillswap.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService implements com.example.skillswap.service.AiService {

    private final WebClient webClient;

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
                        throw new IllegalArgumentException("Scrie câteva idei înainte de generare.");
                }

                String prompt = createPrompt(ideas.trim());

        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "temperature", 0.7,
                "stream", false,
                "messages", List.of(

                        Map.of(
                                "role", "system",
                                "content", "You are an assistant that writes clear and honest profile descriptions for a skill sharing platform."
                        ),

                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
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

    private String createPrompt(String ideas) {

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
}
