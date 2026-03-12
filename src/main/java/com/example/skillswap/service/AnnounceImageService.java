package com.example.skillswap.service;

import java.util.List;
import java.util.Map;

public interface AnnounceImageService {

    String normalizeCategory(String input);

    boolean isAllowedForCategory(String categoryOffered, String imageKey);

    String safePath(String categoryOffered, String imageKey);

    Map<String, List<String>> getSkillImageCatalog();
}
