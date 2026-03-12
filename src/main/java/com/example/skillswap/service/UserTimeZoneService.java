package com.example.skillswap.service;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserTimeZoneService {

    private final UserRepository userRepository;

    @Transactional
    public TimeZoneUpdateResult updateTimeZone(Long userId, String rawTimeZoneId) {
        String normalizedTimeZoneId = normalizeTimeZoneId(rawTimeZoneId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean changed = !Objects.equals(user.getTimeZoneId(), normalizedTimeZoneId);
        if (changed) {
            user.setTimeZoneId(normalizedTimeZoneId);
        }

        return new TimeZoneUpdateResult(user, changed);
    }

    public String normalizeTimeZoneId(String rawTimeZoneId) {
        if (rawTimeZoneId == null || rawTimeZoneId.isBlank()) {
            throw new IllegalArgumentException("Time zone id is required");
        }

        return ZoneId.of(rawTimeZoneId.trim()).getId();
    }

    public record TimeZoneUpdateResult(User user, boolean changed) {
    }
}
