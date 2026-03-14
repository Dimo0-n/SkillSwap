package com.example.skillswap.admin.dto;

import java.time.Instant;

public record CacheRegionStatsDto(
        String cacheName,
        long estimatedSize,
        long requestCount,
        long hitCount,
        double hitRate,
        long missCount,
        double missRate,
        long evictionCount,
        double averageLoadPenaltyNanos,
        Long expireAfterWriteSeconds,
        Long expireAfterAccessSeconds,
        Instant snapshotAt
) {
}