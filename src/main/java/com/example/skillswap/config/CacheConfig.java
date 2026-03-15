package com.example.skillswap.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROFILE_BY_USERNAME_CACHE = "profiles.by-username";
    public static final String PROFILE_BY_USER_ID_CACHE = "profiles.by-user-id";
    public static final String PROFILE_AUTHOR_CACHE = "profiles.author";

    public static final String ANNOUNCE_LATEST5_CACHE = "announces.latest5";
    public static final String ANNOUNCE_LIST_CACHE = "announces.list";
    public static final String ANNOUNCE_BY_ID_CACHE = "announces.by-id";
    public static final String ANNOUNCE_BY_AUTHOR_CACHE = "announces.by-author";

    public static final String CATEGORY_ALL_CACHE = "categories.all";
    public static final String CATEGORY_BY_ID_CACHE = "categories.by-id";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                buildCache(PROFILE_BY_USERNAME_CACHE, Caffeine.newBuilder()
                        .maximumSize(2_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(PROFILE_BY_USER_ID_CACHE, Caffeine.newBuilder()
                        .maximumSize(2_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(PROFILE_AUTHOR_CACHE, Caffeine.newBuilder()
                        .maximumSize(1_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(ANNOUNCE_LATEST5_CACHE, Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(ANNOUNCE_LIST_CACHE, Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(ANNOUNCE_BY_ID_CACHE, Caffeine.newBuilder()
                        .maximumSize(5_000)
                        .expireAfterWrite(3, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(ANNOUNCE_BY_AUTHOR_CACHE, Caffeine.newBuilder()
                        .maximumSize(2_000)
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .recordStats()),
                buildCache(CATEGORY_ALL_CACHE, Caffeine.newBuilder()
                        .maximumSize(20)
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .recordStats()),
                buildCache(CATEGORY_BY_ID_CACHE, Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(12, TimeUnit.HOURS)
                        .recordStats())
        ));
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, Caffeine<Object, Object> cacheDefinition) {
        return new CaffeineCache(name, cacheDefinition.build());
    }
}