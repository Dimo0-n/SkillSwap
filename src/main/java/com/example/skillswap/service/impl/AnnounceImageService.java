package com.example.skillswap.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public final class AnnounceImageService {

    private AnnounceImageService() {}

    // key -> path (source of truth)
    private static final Map<String, String> KEY_TO_PATH = Map.ofEntries(
        Map.entry("prog1", "/img/skill/skill-programming.png"),
        Map.entry("prog2", "/img/skill/skill-programming-2.png"),
        Map.entry("prog3", "/img/skill/skill-programming-3.png"),

        Map.entry("guitar1", "/img/skill/skill-guitar.png"),
        Map.entry("guitar2", "/img/skill/skill-guitar-2.png"),

        Map.entry("eng1", "/img/skill/skill-english.png"),
        Map.entry("eng2", "/img/skill/skill-english-2.png"),

        Map.entry("ps1", "/img/skill/skill-photoshop.png"),
        Map.entry("ps2", "/img/skill/skill-photoshop-2.png"),

        Map.entry("photo1", "/img/skill/skill-photography.png"),
        Map.entry("photo2", "/img/skill/skill-photography-2.png"),

        Map.entry("cook1", "/img/skill/skill-cooking.png"),
        Map.entry("cook2", "/img/skill/skill-cooking-2.png"),

        Map.entry("dance1", "/img/skill/skill-dance.png"),
        Map.entry("dance2", "/img/skill/skill-dance-2.png"),

        Map.entry("speak1", "/img/skill/skill-public-speaking.png"),
        Map.entry("speak2", "/img/skill/skill-public-speaking-2.png"),

        Map.entry("default1", "/img/skill/skill-default.png"),
        Map.entry("default2", "/img/skill/skill-default-2.png")
    );

    // category(normalizată) -> keys permise
    private static final Map<String, Set<String>> CATEGORY_TO_KEYS = Map.ofEntries(
            Map.entry("programare", Set.of("prog1", "prog2", "prog3")),
            Map.entry("programming", Set.of("prog1", "prog2", "prog3")),

            Map.entry("chitara", Set.of("guitar1", "guitar2")),
            Map.entry("guitar", Set.of("guitar1", "guitar2")),

            Map.entry("engleza", Set.of("eng1", "eng2")),
            Map.entry("english", Set.of("eng1", "eng2")),

            Map.entry("photoshop", Set.of("ps1", "ps2")),
            Map.entry("design", Set.of("ps1", "ps2")),

            Map.entry("fotografie", Set.of("photo1", "photo2")),
            Map.entry("photography", Set.of("photo1", "photo2")),

            Map.entry("gatit", Set.of("cook1", "cook2")),
            Map.entry("cooking", Set.of("cook1", "cook2")),

            Map.entry("dans", Set.of("dance1", "dance2")),
            Map.entry("dance", Set.of("dance1", "dance2")),

            Map.entry("prezentare", Set.of("speak1", "speak2")),
            Map.entry("speaking", Set.of("speak1", "speak2"))
    );

    private static final String DEFAULT_KEY = "default1";

    /** Normalizează ca în front: lowercase + fără diacritice + trim */
    public static String normalizeCategory(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase(Locale.ROOT);
        // Scoate diacritice (Java)
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return s;
    }

    /** Verifică dacă imageKey e permis pentru categoria oferită */
    public static boolean isAllowedForCategory(String categoryOffered, String imageKey) {
        String cat = normalizeCategory(categoryOffered);
        Set<String> allowed = CATEGORY_TO_KEYS.get(cat);
        return allowed != null && imageKey != null && allowed.contains(imageKey);
    }

    /** Îți dă path-ul sigur, fallback la default */
    public static String safePath(String categoryOffered, String imageKey) {
        if (!isAllowedForCategory(categoryOffered, imageKey)) {
            return KEY_TO_PATH.get(DEFAULT_KEY);
        }
        return KEY_TO_PATH.getOrDefault(imageKey, KEY_TO_PATH.get(DEFAULT_KEY));
    }
}
