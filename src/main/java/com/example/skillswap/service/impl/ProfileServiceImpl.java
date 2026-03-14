package com.example.skillswap.service.impl;

import com.example.skillswap.config.CacheConfig;
import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.event.ProfileReputationRefreshRequestedEvent;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfileImageStorageService;
import com.example.skillswap.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "/img/default-avatar.png";

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.skillswap.service.ProfileCompletionService profileCompletionService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ProfileImageStorageService profileImageStorageService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @Transactional
    public void saveProfile(ProfilDto profilDto, MultipartFile profilePicture, String email) throws IOException {

        User user = Optional.ofNullable(userRepository.findUserByEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profil toSave = profileRepository.findFirstByUserEmailOrderByIdDesc(email)
                .orElseGet(Profil::new);

        toSave.setUser(user);
        toSave.setName(normalizeNullableText(profilDto.getName()));
        toSave.setProfession(normalizeNullableText(profilDto.getProfession()));
        toSave.setBioShort(normalizeNullableText(profilDto.getBioShort()));
        toSave.setCompleteDescription(normalizeNullableText(profilDto.getCompleteDescription()));
        toSave.setAvailabilityMask(profilDto.getAvailabilityMask());
        toSave.setLimits(normalizeCommaSeparated(profilDto.getLimits()));
        toSave.setCompetences(normalizeCommaSeparated(profilDto.getCompetences()));
        toSave.setStrengths(normalizeCommaSeparated(profilDto.getStrengths()));

        if (profilePicture != null && !profilePicture.isEmpty()) {
            toSave.setImageUrl(profileImageStorageService.uploadProfileImage(profilePicture, user.getId()));
        }

        Profil savedProfile = profileRepository.save(toSave);
        profileCompletionService.refreshProfileCompletion(user, savedProfile);
        applicationEventPublisher.publishEvent(new ProfileReputationRefreshRequestedEvent(user.getId()));
        evictProfileCaches(user.getId(), email);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PROFILE_BY_USERNAME_CACHE, key = "#username")
    public ProfilDto getProfileForView(String username) {

        Profil profil = profileRepository
                .findFirstByUserEmailOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        return mapToDto(profil);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PROFILE_BY_USER_ID_CACHE, key = "#userId")
    public ProfilDto getProfileForUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileRepository.findFirstByUserIdOrderByIdDesc(userId)
                .map(this::mapToDto)
                .orElseGet(() -> mapUserToDto(user));
    }

    private ProfilDto mapToDto(Profil profil) {
        ProfilDto dto = new ProfilDto();

        dto.setId(profil.getId());
        dto.setUserId(profil.getUser().getId());
        dto.setName(profil.getName());
        dto.setProfession(profil.getProfession());
        dto.setBioShort(profil.getBioShort());
        dto.setCompleteDescription(profil.getCompleteDescription());

        dto.setCompetences(profil.getCompetences());
        dto.setStrengths(profil.getStrengths());
        dto.setLimits(profil.getLimits());
        dto.setAvailabilityMask(profil.getAvailabilityMask());
        dto.setReputationScore(profil.getReputationScore());
        dto.setReputationSummary(profil.getReputationSummary());
        dto.setFeedbackCountAtLastEvaluation(profil.getFeedbackCountAtLastEvaluation());
        dto.setImageUrl(resolveProfileImageUrl(profil));

        return dto;
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PROFILE_AUTHOR_CACHE, key = "#userId")
    public ProfilDto getAuthorByUserId(Long userId) {
        return getProfileForUserId(userId);
    }

    private void evictProfileCaches(Long userId, String username) {
        if (username != null) {
            java.util.Optional.ofNullable(cacheManager.getCache(CacheConfig.PROFILE_BY_USERNAME_CACHE))
                    .ifPresent(cache -> cache.evict(username));
        }

        if (userId != null) {
            java.util.Optional.ofNullable(cacheManager.getCache(CacheConfig.PROFILE_BY_USER_ID_CACHE))
                    .ifPresent(cache -> cache.evict(userId));
            java.util.Optional.ofNullable(cacheManager.getCache(CacheConfig.PROFILE_AUTHOR_CACHE))
                    .ifPresent(cache -> cache.evict(userId));
        }
    }

    private ProfilDto mapUserToDto(User user) {
        ProfilDto dto = new ProfilDto();
        dto.setUserId(user.getId());
        dto.setName(user.getFullName());
        dto.setProfession("SkillSwap user");
        dto.setImageUrl(DEFAULT_PROFILE_IMAGE_URL);
        return dto;
    }

    private String resolveProfileImageUrl(Profil profil) {
        String imageUrl = profil.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            return DEFAULT_PROFILE_IMAGE_URL;
        }

        return imageUrl;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.lines()
                .flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse(null);
    }

}
