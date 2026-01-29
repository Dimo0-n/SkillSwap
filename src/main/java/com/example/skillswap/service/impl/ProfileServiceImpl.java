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

        profileRepository.save(toSave);

    }

    @Override
    public ProfilDto getProfileForView(String username) {

        Profil profil = profileRepository
                .findFirstByUserEmailOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        ProfilDto dto = new ProfilDto();

        dto.setName(profil.getName());
        dto.setProfession(profil.getProfession());
        dto.setBioShort(profil.getBioShort());
        dto.setCompleteDescription(profil.getCompleteDescription());

        dto.setCompetences(profil.getCompetences());
        dto.setStrengths(profil.getStrengths());
        dto.setLimits(profil.getLimits());
        dto.setAvailabilityMask(profil.getAvailabilityMask());

        dto.setImageUrl("/profile/image/" + username);

        return dto;
    }


    public byte[] getProfileImageByEmail(String email) throws IOException {
        Profil profil = profileRepository
                .findFirstByUserEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        if (profil.getImage() == null) {
            ClassPathResource resource = new ClassPathResource("static/img/default-avatar.png");
            try (InputStream is = resource.getInputStream()) {
                return is.readAllBytes();
            }
        }
        return profil.getImage();
    }



//    //pentru citire Availability
//public Set<Availability> getAvailability() {
//    Set<Availability> result = EnumSet.noneOf(Availability.class);
//
//    for (Availability a : Availability.values()) {
//        if ((availability & a.getBit()) != 0) {
//            result.add(a);
//        }
//    }
//    return result;
//}


}
