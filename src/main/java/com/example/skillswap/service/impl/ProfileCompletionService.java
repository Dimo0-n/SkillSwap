package com.example.skillswap.service.impl;

import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class ProfileCompletionService implements com.example.skillswap.service.ProfileCompletionService {

    private final UserRepository userRepository;

    public boolean calculateProfileCompletion(Profil profile) {
        return profile != null
                && hasText(profile.getName())
                && hasText(profile.getBioShort())
                && hasText(profile.getCompetences())
                && hasText(profile.getCompleteDescription());
    }

    @Transactional
    public boolean refreshProfileCompletion(User user, Profil profile) {
        boolean profileCompleted = calculateProfileCompletion(profile);
        user.setProfileCompleted(profileCompleted);
        userRepository.save(user);
        return profileCompleted;
    }

    @Transactional(readOnly = true)
    public boolean isProfileCompleted(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return false;
        }

        return userRepository.findByEmail(principal.getName())
                .map(User::isProfileCompleted)
                .orElse(false);
    }

    public boolean isProfileCompleted(User user) {
        return user != null && user.isProfileCompleted();
    }

    public String getRequiredRedirectView() {
        return "redirect:" + REQUIRED_REDIRECT_PATH;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
