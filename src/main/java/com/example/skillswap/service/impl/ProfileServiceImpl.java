package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.event.ProfileReputationRefreshRequestedEvent;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfileImageStorageService;
import com.example.skillswap.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public void saveProfile(ProfilDto profilDto, MultipartFile profilePicture, String email) throws IOException {

        User user = Optional.ofNullable(userRepository.findUserByEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profil toSave = profileRepository.findFirstByUserEmailOrderByIdDesc(email)
                .orElseGet(Profil::new);

        toSave.setUser(user);
        toSave.setName(profilDto.getName());
        toSave.setProfession(profilDto.getProfession());
        toSave.setBioShort(profilDto.getBioShort());
        toSave.setCompleteDescription(profilDto.getCompleteDescription());
        toSave.setAvailabilityMask(profilDto.getAvailabilityMask());
        toSave.setLimits(profilDto.getLimits());
        toSave.setCompetences(profilDto.getCompetences());
        toSave.setStrengths(profilDto.getStrengths());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            toSave.setImageUrl(profileImageStorageService.uploadProfileImage(profilePicture, user.getId()));
        }

        Profil savedProfile = profileRepository.save(toSave);
        profileCompletionService.refreshProfileCompletion(user, savedProfile);
        applicationEventPublisher.publishEvent(new ProfileReputationRefreshRequestedEvent(user.getId()));
    }

    @Override
    public ProfilDto getProfileForView(String username) {

        Profil profil = profileRepository
                .findFirstByUserEmailOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        return mapToDto(profil);
    }

    @Override
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
    public ProfilDto getAuthorByUserId(Long userId) {
        return getProfileForUserId(userId);
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

}
