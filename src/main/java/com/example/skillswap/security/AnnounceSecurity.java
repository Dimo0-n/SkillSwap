package com.example.skillswap.security;

import com.example.skillswap.repository.AnnounceRepository;
import org.springframework.security.core.Authentication;
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

        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        Long userId = cud.getId();

        return announceRepository
                .existsByIdAndUserId(announceId, userId);
    }
}

