package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileCompletionService profileCompletionService;

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
            toSave.setImage(profilePicture.getBytes());
        }

        Profil savedProfile = profileRepository.save(toSave);
        profileCompletionService.refreshProfileCompletion(user, savedProfile);
    }

    @Override
    public ProfilDto getProfileForView(String username) {

        Profil profil = profileRepository
                .findFirstByUserEmailOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        return mapToDto(profil, username);
    }

    @Override
    public ProfilDto getProfileForUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileRepository.findFirstByUserIdOrderByIdDesc(userId)
                .map(profil -> mapToDto(profil, user.getEmail()))
                .orElseGet(() -> mapUserToDto(user));
    }

    public byte[] getProfileImageByEmail(String email) throws IOException {
        Optional<Profil> profil = profileRepository.findFirstByUserEmailOrderByIdDesc(email);

        if (profil.isPresent() && profil.get().getImage() != null) {
            return profil.get().getImage();
        }

        return loadDefaultAvatar();
    }

    private ProfilDto mapToDto(Profil profil, String email) {
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

        // Add a cache-busting param so the browser fetches the latest avatar after updates
        String version = String.valueOf(
                (profil.getImage() != null ? profil.getImage().length : 0) + profil.getId());
        dto.setImageUrl("/profile/image/" + email + "?v=" + version);

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
        dto.setImageUrl("/profile/image/" + user.getEmail());
        return dto;
    }

    private byte[] loadDefaultAvatar() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/img/default-avatar.png");
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }

}
