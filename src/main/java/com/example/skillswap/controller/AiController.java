package com.example.skillswap.controller;

import com.example.skillswap.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-description")
    public ResponseEntity<String> generateDescription(@RequestBody Map<String, String> body) {
        String ideas = body.getOrDefault("ideas", "").trim();

        if (ideas.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scrie câteva idei înainte de generare.");
        }

        if (ideas.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Textul este prea lung. Limita este 2000 de caractere.");
        }

        try {
            String description = aiService.generateDescription(ideas);
            return ResponseEntity.ok(description);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Serviciul AI nu este disponibil momentan. Încearcă din nou.", ex);
        }
    }

}
