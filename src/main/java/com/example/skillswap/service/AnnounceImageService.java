package com.example.skillswap.service;

public interface AnnounceImageService {

    String normalizeCategory(String input);

    boolean isAllowedForCategory(String categoryOffered, String imageKey);

    String safePath(String categoryOffered, String imageKey);
}
