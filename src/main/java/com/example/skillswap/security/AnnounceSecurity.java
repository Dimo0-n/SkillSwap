package com.example.skillswap.security;

import com.example.skillswap.repository.AnnounceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component("announceSecurity")
public class AnnounceSecurity {

    private final AnnounceRepository announceRepository;

    public AnnounceSecurity(AnnounceRepository announceRepository) {
        this.announceRepository = announceRepository;
    }

    //functie de securitate pentru a afla daca user-ul
    // cu adevarat este proprietarul anuntului inainte de stergere
    public boolean isOwner(Long announceId, Authentication auth) {

        if (auth == null) {
            return false;
        }

        Long userId = extractCurrentUserId(auth);
        if (userId == null) {
            return false;
        }

        return announceRepository
                .existsByIdAndUserId(announceId, userId);
    }

    private Long extractCurrentUserId(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        if (principal instanceof OAuth2User oauth2User) {
            Object userId = oauth2User.getAttribute("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
        }

        return null;
    }
}

