package com.example.skillswap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    @PostMapping("/ping")
    public ResponseEntity<Void> ping() {
        // Authentication filter updates user presence state for this request.
        return ResponseEntity.noContent().build();
    }
}
