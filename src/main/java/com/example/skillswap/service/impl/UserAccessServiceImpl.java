package com.example.skillswap.service.impl;

import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.UserAccessService;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccessServiceImpl implements UserAccessService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordSuccessfulLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(UtcDateTimes.now());
            user.setLastSeenAt(UtcDateTimes.now());
            user.setOnline(true);
        });
    }
}