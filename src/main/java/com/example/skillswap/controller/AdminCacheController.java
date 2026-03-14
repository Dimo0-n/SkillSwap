package com.example.skillswap.controller;

import com.example.skillswap.admin.dto.CacheRegionStatsDto;
import com.example.skillswap.admin.service.AdminCacheStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api/cache")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCacheController {

    private final AdminCacheStatsService adminCacheStatsService;

    @GetMapping("/stats")
    public ResponseEntity<List<CacheRegionStatsDto>> allStats() {
        return ResponseEntity.ok(adminCacheStatsService.getAllCacheStats());
    }

    @GetMapping("/stats/{cacheName}")
    public ResponseEntity<CacheRegionStatsDto> statsByName(@PathVariable String cacheName) {
        return adminCacheStatsService.getCacheStatsByName(cacheName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}