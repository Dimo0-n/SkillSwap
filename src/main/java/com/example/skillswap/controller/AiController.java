package com.example.skillswap.controller;

import com.example.skillswap.service.impl.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/generate-description")
    public ResponseEntity<String> generateDescription(@RequestBody Map<String, String> body) {

        String ideas = body.get("ideas");

        String description = aiService.generateDescription(ideas);

        return ResponseEntity.ok(description);
    }

}