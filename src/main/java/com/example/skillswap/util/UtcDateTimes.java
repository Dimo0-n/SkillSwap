package com.example.skillswap.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class UtcDateTimes {

    private UtcDateTimes() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Instant toInstant(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return value.toInstant(ZoneOffset.UTC);
    }
}
