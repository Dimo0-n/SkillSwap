package com.example.skillswap.admin.service.impl;

import com.example.skillswap.admin.dto.CacheRegionStatsDto;
import com.example.skillswap.admin.service.AdminCacheStatsService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdminCacheStatsServiceImpl implements AdminCacheStatsService {

    private final CacheManager cacheManager;

    @Override
    public List<CacheRegionStatsDto> getAllCacheStats() {
        return cacheManager.getCacheNames().stream()
                .map(this::buildStats)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CacheRegionStatsDto::cacheName))
                .toList();
    }

    @Override
    public Optional<CacheRegionStatsDto> getCacheStatsByName(String cacheName) {
        return buildStats(cacheName);
    }

    private Optional<CacheRegionStatsDto> buildStats(String cacheName) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (!(springCache instanceof CaffeineCache caffeineCache)) {
            return Optional.empty();
        }

        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        CacheStats stats = nativeCache.stats();
        Policy<Object, Object> policy = nativeCache.policy();

        Long expireAfterWriteSeconds = policy.expireAfterWrite()
                .map(expiration -> expiration.getExpiresAfter(TimeUnit.SECONDS))
                .orElse(null);

        Long expireAfterAccessSeconds = policy.expireAfterAccess()
                .map(expiration -> expiration.getExpiresAfter(TimeUnit.SECONDS))
                .orElse(null);

        return Optional.of(new CacheRegionStatsDto(
                cacheName,
                nativeCache.estimatedSize(),
                stats.requestCount(),
                stats.hitCount(),
                stats.hitRate(),
                stats.missCount(),
                stats.missRate(),
                stats.evictionCount(),
                stats.averageLoadPenalty(),
                expireAfterWriteSeconds,
                expireAfterAccessSeconds,
                Instant.now()
        ));
    }
}