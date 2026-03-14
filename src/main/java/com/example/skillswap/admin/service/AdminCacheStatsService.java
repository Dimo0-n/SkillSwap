package com.example.skillswap.admin.service;

import com.example.skillswap.admin.dto.CacheRegionStatsDto;

import java.util.List;
import java.util.Optional;

public interface AdminCacheStatsService {

    List<CacheRegionStatsDto> getAllCacheStats();

    Optional<CacheRegionStatsDto> getCacheStatsByName(String cacheName);
}