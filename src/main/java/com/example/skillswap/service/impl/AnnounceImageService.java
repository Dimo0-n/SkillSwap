package com.example.skillswap.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnnounceImageService implements com.example.skillswap.service.AnnounceImageService {

    // key -> path (source of truth)
    private static final Map<String, String> KEY_TO_PATH = Map.ofEntries(
        Map.entry("prog1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394304/skill-programming_gmiwi9.png"),
        Map.entry("prog2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400801/skill-programming-2_o7oxn2.jpg"),
        Map.entry("prog3", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400804/skill-programming3_v2hd16.jpg"),

        Map.entry("guitar1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394301/skill-guitar_vi3kqm.png"),
        Map.entry("guitar2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400794/skill-guitare2_ssnvnj.jpg"),

        Map.entry("eng1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394300/skill-english_flxmr0.png"),
        Map.entry("eng2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400789/skill-english2_mff1kw.jpg"),

        Map.entry("ps1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394303/skill-photoshop_wiyk5f.png"),
        Map.entry("ps2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400799/skill-photoshop2_jtoovv.jpg"),

        Map.entry("photo1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394302/skill-photography_mbasvx.png"),
        Map.entry("photo2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400795/skill-photography2_chjfrk.jpg"),

        Map.entry("cook1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394298/skill-cooking_tslvh1.png"),
        Map.entry("cook2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400782/skill-cooking2_nej88w.jpg"),

        Map.entry("dance1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394299/skill-dance_skkhv8.png"),
        Map.entry("dance2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400785/skill-dance2_iee29c.jpg"),

        Map.entry("speak1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394306/skill-public-speaking_rhgjua.png"),
        Map.entry("speak2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400807/skill-speaking2_uxjyam.jpg"),

        Map.entry("default1", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773394300/skill-default_ofzyc1.png"),
        Map.entry("default2", "https://res.cloudinary.com/dsqtynmrn/image/upload/v1773400788/skill-default2_ck37b9.jpg")
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
    @Override
    public String normalizeCategory(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase(Locale.ROOT);
        // Scoate diacritice (Java)
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return s;
    }

    /** Verifică dacă imageKey e permis pentru categoria oferită */
    @Override
    public boolean isAllowedForCategory(String categoryOffered, String imageKey) {
        String cat = normalizeCategory(categoryOffered);
        Set<String> allowed = CATEGORY_TO_KEYS.get(cat);
        return allowed != null && imageKey != null && allowed.contains(imageKey);
    }

    /** Îți dă path-ul sigur, fallback la default */
    @Override
    public String safePath(String categoryOffered, String imageKey) {
        if (!isAllowedForCategory(categoryOffered, imageKey)) {
            return KEY_TO_PATH.get(DEFAULT_KEY);
        }
        return KEY_TO_PATH.getOrDefault(imageKey, KEY_TO_PATH.get(DEFAULT_KEY));
    }

    @Override
    public Map<String, List<String>> getSkillImageCatalog() {
        Map<String, List<String>> catalog = new LinkedHashMap<>();
        CATEGORY_TO_KEYS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> catalog.put(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted()
                                .map(key -> KEY_TO_PATH.getOrDefault(key, KEY_TO_PATH.get(DEFAULT_KEY)))
                                .distinct()
                                .toList()
                ));
        return catalog;
    }
}
