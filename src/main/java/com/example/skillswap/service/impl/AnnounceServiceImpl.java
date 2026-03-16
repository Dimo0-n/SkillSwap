package com.example.skillswap.service.impl;

import com.example.skillswap.config.CacheConfig;
import com.example.skillswap.dto.AnnounceDto;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AnnounceStatus;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.AnnounceImageService;
import com.example.skillswap.service.AnnounceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnounceServiceImpl implements AnnounceService {

    @Autowired
    private AnnounceRepository announceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnounceImageService announceImageService;

    //5 anunturi care sunt afisate pentru pagina de index
    @Override
    @Cacheable(cacheNames = CacheConfig.ANNOUNCE_LATEST5_CACHE, key = "'latest5'")
    public List<Announce> getLatest5Announces() {
        List<Announce> latest5Announces = announceRepository.findAll().stream()
                .filter(announce -> !announce.isDeletedByAdmin())
                .filter(announce -> announce.getStatus() == AnnounceStatus.ACTIVE)
                .peek(this::normalizeImagePathForDisplay)
                .toList();
        return latest5Announces.subList(0, Math.min(5, latest5Announces.size()));
    }

    //lista cu toate anunturile care sunt afisate din DB
    @Override
    @Cacheable(cacheNames = CacheConfig.ANNOUNCE_LIST_CACHE, key = "'all'")
    public List<Announce> getAnnouncesList() {
        return announceRepository.findAllByOrderByIdDesc().stream()
                .filter(announce -> !announce.isDeletedByAdmin())
                .filter(announce -> announce.getStatus() == AnnounceStatus.ACTIVE)
                .peek(this::normalizeImagePathForDisplay)
                .toList();
    }

    //Crearea unui anunt nou de catre user-ul logat
    @Override
        @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_LATEST5_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_LIST_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_BY_AUTHOR_CACHE, allEntries = true)
        })
    public void save(AnnounceDto announceDto, Authentication auth) {

        User user = Optional.ofNullable(userRepository.findUserByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Announce announce = new Announce();

        announce.setTitle(normalizeNullableText(announceDto.getTitle()));
        announce.setDescription(normalizeNullableText(announceDto.getDescription()));
        announce.setAuthor(normalizeNullableText(announceDto.getAuthor()));
        announce.setCategoryOffered(normalizeNullableText(announceDto.getCategoryOffered()));
        announce.setCategoryRequired(normalizeNullableText(announceDto.getCategoryRequired()));
        announce.setImageKey(announceDto.getImageKey());
        announce.setImagePath(announceDto.getImagePath());
        announce.setAdditionalInfo(normalizeNullableText(announceDto.getAdditionalInfo()));
        announce.setDate(LocalDateTime.now());
        announce.setStatus(AnnounceStatus.ACTIVE);
        announce.setUser(user);

        announceRepository.save(announce);
    }

    //afisarea anunturilor pe care le-a publicat user-ul
    @Override
    @Cacheable(cacheNames = CacheConfig.ANNOUNCE_BY_AUTHOR_CACHE, key = "#id")
    public List<Announce> getAnnouncesListByEmail(Long id) {
        return announceRepository.getAnnouncesListByEmail(id).stream()
                .filter(announce -> !announce.isDeletedByAdmin())
                .peek(this::normalizeImagePathForDisplay)
                .toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_BY_ID_CACHE, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_LATEST5_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_LIST_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ANNOUNCE_BY_AUTHOR_CACHE, allEntries = true)
    })
    public void deleteAnnounceById(Long id) {
        announceRepository.deleteById(id);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.ANNOUNCE_BY_ID_CACHE, key = "#id")
    public AnnounceDto getAnnounceById(Long id) {

        Optional<Announce> announce = announceRepository.findById(id);
        if (announce.isEmpty() || announce.get().isDeletedByAdmin()) {
            throw new RuntimeException("Announce not found");
        }
        AnnounceDto announceDto = new AnnounceDto();

        announceDto.setId(announce.get().getId());
        announceDto.setTitle(announce.get().getTitle());
        announceDto.setDescription(announce.get().getDescription());
        announceDto.setAuthor(announce.get().getAuthor());
        announceDto.setCategoryOffered(announce.get().getCategoryOffered());
        announceDto.setCategoryRequired(announce.get().getCategoryRequired());
        announceDto.setImageKey(announce.get().getImageKey());
        announceDto.setImagePath(resolveImagePathForDisplay(announce.get()));
        announceDto.setAdditionalInfo(announce.get().getAdditionalInfo());
        announceDto.setDate(announce.get().getDate());
        announceDto.setUserId(announce.get().getUser().getId());

        return announceDto;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void normalizeImagePathForDisplay(Announce announce) {
        announce.setImagePath(resolveImagePathForDisplay(announce));
    }

    private String resolveImagePathForDisplay(Announce announce) {
        String imagePath = announce.getImagePath();
        if (imagePath != null) {
            String trimmedPath = imagePath.trim();
            if (!trimmedPath.isEmpty() && !trimmedPath.startsWith("/img/skill/")) {
                return trimmedPath;
            }
        }

        return announceImageService.safePath(announce.getCategoryOffered(), announce.getImageKey());
    }


}

